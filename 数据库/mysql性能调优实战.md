## 性能优化实战

### mysql碎片

MySQL 的碎片是 MySQL 运维过程中比较常见的问题，碎片的存在十分影响数据库的性能，本文将对 MySQL 碎片进行一次讲解。

**判断方法：**

MySQL 的碎片是否产生，通过查看

```
show table status from db_name\G; 
```

这个命令中 Data_free 字段，如果该字段不为 0，则产生了数据碎片。

**产生的原因：**

**1. 经常进行 delete 操作**

经常进行 delete 操作，产生空白空间，如果进行新的插入操作，MySQL将尝试利用这些留空的区域，但仍然无法将其彻底占用，久而久之就产生了碎片；

**演示：**

创建一张表，往里面插入数据，进行一个带有 where 条件或者 limit 的 delete 操作，删除前后对比一下 Data_free 的变化。

删除前：

![img](https://opensource.actionsky.com/wp-content/uploads/2019/07/%E5%9B%BE1.png)

删除后：

![img](https://opensource.actionsky.com/wp-content/uploads/2019/07/%E5%9B%BE2.png)

Data_free 不为 0，说明有碎片；

**2. update 更新**

update 更新可变长度的字段(例如 varchar 类型)，将长的字符串更新成短的。之前存储的内容长，后来存储是短的，即使后来插入新数据，那么有一些空白区域还是没能有效利用的。

**演示：**

创建一张表，往里面插入一条数据，进行一个 update 操作，前后对比一下 Data_free 的变化。

```
CREATE TABLE `t1` ( `k` varchar(3000) DEFAULT NULL ) ENGINE=MyISAM DEFAULT CHARSET=utf8; 
```

更新语句：update t1 set k=’aaa’;

更新前长度：223 Data_free：0

更新后长度：3 Data_free：204

Data_free 不为 0，说明有碎片；

**产生影响：**

1. 由于碎片空间是不连续的，导致这些空间不能充分被利用；

2. 由于碎片的存在，导致数据库的磁盘 I/O 操作变成离散随机读写，加重了磁盘 I/O 的负担。

**清理办法：**

- MyISAM：optimize table 表名；（OPTIMIZE 可以整理数据文件,并重排索引）
- Innodb：

1. ALTER TABLE tablename ENGINE=InnoDB；(重建表存储引擎，重新组织数据) 

2. 进行一次数据的导入导出

**碎片清理的性能对比：**

引用我之前一个生产库的数据，对比一下清理前后的差异。

**空间对比：**

| 库名       | 清理前大小 | 清理后大小 |
| :--------- | :--------- | :--------- |
| facebook   | 2.2G       | 1.1G       |
| instagram  | 40G        | 22G        |
| linkedin   | 555M       | 208M       |
| googleplus | 19G        | 8.4G       |
| twitter    | 107G       | 44G        |

SQL执行速度：

```
select count(*) from test.twitter_11; 
```

修改前：1 row in set (7.37 sec)

修改后：1 row in set (1.28 sec)

#### 索引实战

1.使用前缀索引，order_num 建了普通索引，可以看到使用前缀还是走了索引，但是由于select * 导致需要回表

![image-20200601201851488](https://github.com/rainluacgq/java/blob/master/数据库/pic/image-20200601201851488.png)

2.只检索建了索引的order_num字段，可以看到不需要回表。

![image-20200601200759913](https://github.com/rainluacgq/java/blob/master/数据库/pic/image-20200601201413849.png)

3.未使用前缀索引的情况。

![image-20200601201800352](https://github.com/rainluacgq/java/blob/master/数据库/pic/image-20200601201800352.png)

4.使用主键查找

![20200602212552](https://github.com/rainluacgq/java/blob/master/数据库/pic/20200602212552.png)

![20200602212545](https://github.com/rainluacgq/java/blob/master/数据库/pic/20200602212545.png)

id 序号
**select_type**

- simple ：即简单select 查询，不包含union及子查询；
  primary ：最外层的 select 查询；
  union ：表示此查询是 union 的第二或随后的查询；
  dependent union：union 中的第二个或后面的查询语句, 取决于外面的查询；
  union result ：union的结果；
  subquery ：子查询中的第一个select；
  dependent subquery：子查询中的第一个select，取决于外面的查询，即子查询依赖于外层查询的结果。

**type**

性能从好到坏依次如下：
system：表中只有一条数据，这是一个特殊的const 类型；
const：针对主键或唯一索引的等值查询扫描，最多只返回一行数据，const 查询速度非常快，因为它仅仅读取一次即可；
eq_ref：此类型通常出现在多表的 join 查询，表示对于前表的每一个结果,都只能匹配到后表的一行结果，并且查询的比较操作通常是＝, 查询效率较高；
ref：此类型通常出现在多表的 join 查询, 针对于非唯一或非主键索引, 或者是使用了 最左前缀 规则索引的查询；
fulltext：全文索引检索，要注意，全文索引的优先级很高，若全文索引和普通索引同时存在时，mysql不管代价，优先选择使用全文索引；
ref_or_null：与ref方法类似，只是增加了null值的比较。实际用的不多；
unique_subquery：用于where中的in形式子查询，子查询返回不重复值唯一值；
index_subquery：用于in形式子查询使用到了辅助索引或者in常数列表，子查询可能返回重复值，可以使用索引将子查询去重；
index_merge：表示查询使用了两个以上的索引，最后取交集或者并集，常见and,or的条件使用了不同的索引，官方排序这个在ref_or_null之后，但是实际上由于要读取所个索引，性能可能大部分时间都不如range；
range：表示使用索引范围查询，通过索引字段范围获取表中部分数据记录。这个类型通常出现在 =, <>, >, >=, <, <=, IS NULL, <=>, BETWEEN, IN操作中，此时输出的 ref 字段为 NULL并且key_len字段是此次查询中使用到的索引的最长的那个；
index：全表扫描，只是扫描表的时候按照索引次序进行而不是行。主要优点就是避免了排序，但是开销仍然非常大，这种情况时, Extra 字段会显示 Using index；
all：性能最差的情况，使用了全表扫描，系统必须避免出现这种情况。

**possible_keys**

显示可能应用在这张表中的索引,一个或多个,查询到的索引不一定是真正被用到的
**key**

实际使用的索引,如果为null,则没有使用索引,因此会出现possible_keys列有可能被用到的索引,但是key列为null,表示实际没用索引
**key_len**

表示索引中使用的字节数,而通过该列计算查询中使用的 索引长度,在不损失精确性的情况下,长度越短越好,key_len显示的值为索引字段的最大可能长度,并非实际使用长度,即,key_len是根据表定义计算而得么不是通过表内检索出的
**ref**

显示索引的哪一列被使用了,如果可能的话是一个常数,哪些列或常量被用于查找索引列上的值
**rows**

扫描了多少行数，也是性能评估的重要依据
**extra**

Using filesort:说明mysql会对数据使用一个外部的索引排序,而不是按照表内的索引顺序进行读取,mysql中无法利用索引完成的排序操作称为"文件排序"
Using temporary :使用了临时表保存中间结果,mysql在对查询结果排序时使用临时表,常见于order by和分组查询group by
Using index:表示相应的select操作中使用了覆盖索引（Covering Index），避免访问了表的数据行，效率不错。如果同时出现using where，表明索引被用来执行索引键值的查找；如果没有同时出现using where，表明索引用来读取数据而非执行查找动作。 其中的覆盖索引含义是所查询的列是和建立的索引字段和个数是一一对应的
Using where:表明使用了where过滤
Using join buffer:表明使用了连接缓存,如在查询的时候会有多次join,则可能会产生临时表
impossible where:表示where子句的值总是false,不能用来获取任何元组
select tables optimized away
在没有GROUPBY子句的情况下，基于索引优化MIN/MAX操作或者对于MyISAM存储引擎优化COUNT(*)操作，不必等到执行阶段再进行计算，查询执行计划生成的阶段即完成优化。
distinct
优化distinct操作，在找到第一匹配的元组后即停止找同样值的动作

### 慢query优化实战

```sql
SHOW VARIABLES LIKE '%query%'
```

![image-20200702104518134](https://github.com/rainluacgq/java/blob/master/数据库/pic/image-20200702104518134.png)

开启慢查询：

1.永久开启修改mysql 下的mysql.cnf配置文件

```ini
### 必须指定file或者是table如果是table则慢查询信息会保存到mysql库下的slow_log表中
log_output=file
###用于指定是否打开慢查询日志 
slow_query_log=on 
###慢查询日志文件路径 
slow_query_log_file = /var/log/mysql/mysql-slow.log
###超过多少秒的查询就写入日志 
long_query_time = 1
```

2.临时开启

"SET GLOBAL SLOW_QUERY_LOG=ON

查看是否设置成功：

![](https://github.com/rainluacgq/java/blob/master/数据库/pic/image-20200702111836798.png)

查看慢日志：

```sql
mysqld, Version: 5.7.29-log (MySQL Community Server (GPL)). started with:
Tcp port: 0  Unix socket: (null)
Time                 Id Command    Argument

# Time: 2020-07-02T03:28:41.701401Z

# User@Host: root[root] @  [172.17.0.1]  Id:     3

# Query_time: 1.013697  Lock_time: 0.000146 Rows_sent: 1  Rows_examined: 171083

use mall;
SET timestamp=1593660521;
select count(*) from mp_nand_info
```

![image-20200702135643928](https://github.com/rainluacgq/java/blob/master/数据库/pic/image-20200702135643928.png)

select count(*)是mysql默认支持的查询语句，会自行进行优化，可以看到使用了索引。

发现走的是全表

![image-20200702134803620](https://github.com/rainluacgq/java/blob/master/数据库/pic/image-20200702134803620.png)

![image-20200702135118515](https://github.com/rainluacgq/java/blob/master/数据库/pic/image-20200702135118515.png)

对commit字段新建索引

![image-20200702135158362](https://github.com/rainluacgq/java/blob/master/数据库/pic/image-20200702135158362.png)

查询速度明显提升：

![image-20200702135243259](https://github.com/rainluacgq/java/blob/master/数据库/pic/image-20200702135243259.png)