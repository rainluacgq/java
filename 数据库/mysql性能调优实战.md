### mysql性能调优

#### mysql性能优化

- MySQL性能

- - 最大数据量

- 《阿里巴巴Java开发手册》提出单表行数超过500万行或者单表容量超过2GB，才推荐分库分表。性能由综合因素决定，抛开业务复杂度，影响程度依次是硬件配置、MySQL配置、数据表设计、索引优化。500万这个值仅供参考，并非铁律。

- - 最大并发数

    并发数是指同一时刻数据库能处理多少个请求，由max_connections和max_user_connections决定。max_connections是指MySQL实例的最大连接数，上限值是16384，max_user_connections是指每个数据库用户的最大连接数。

    MySQL会为每个连接提供缓冲区，意味着消耗更多的内存。如果连接数设置太高硬件吃不消，太低又不能充分利用硬件。一般要求两者比值超过10%，计算方法如下：

    ```mysql
    max_used_connections / max_connections * 100% = 3/100 *100% ≈ 3%
    ```

  - 查询耗时0.5秒

- 建议将单次查询耗时控制在0.5秒以内，0.5秒是个经验值，源于用户体验的3秒原则。如果用户的操作3秒内没有响应，将会厌烦甚至退出。响应时间=客户端UI渲染耗时+网络请求耗时+应用程序处理耗时+查询数据库耗时，0.5秒就是留给数据库1/6的处理时间。

- - 实施原则
    - 充分利用但不滥用索引，须知索引也消耗磁盘和CPU。
    - 不推荐使用数据库函数格式化数据，交给应用程序处理。
    - 不推荐使用外键约束，用应用程序保证数据准确性。
    - 写多读少的场景，不推荐使用唯一索引，用应用程序保证唯一性。
    - 适当冗余字段，尝试创建中间表，用应用程序计算中间结果，用空间换时间。
    - 不允许执行极度耗时的事务，配合应用程序拆分成更小的事务。
    - 预估重要数据表（比如订单表）的负载和数据增长态势，提前优化。

- 数据表设计

- - 数据类型

- 数据类型的选择原则：更简单或者占用空间更小。

- 如果长度能够满足，整型尽量使用tinyint、smallint、medium_int而非int。

- 如果字符串长度确定，采用char类型。

- 如果varchar能够满足，不采用text类型。

- 精度要求较高的使用decimal类型，也可以使用BIGINT，比如精确两位小数就乘以100后保存。

- 尽量采用timestamp而非datetime。

- ![img](https://mmbiz.qpic.cn/mmbiz_png/JfTPiahTHJhoSNMK1rQe1nMzP7wHQpYxZpBpcFjJSa7Jskae0kzic1I7hO5MWC265XUb1hKh1H9bFxvq6JU9ho2g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

- 相比datetime，timestamp占用更少的空间，以UTC的格式储存自动转换时区。

- - 避免空值

    MySQL中字段为NULL时依然占用空间，会使索引、索引统计更加复杂。从NULL值更新到非NULL无法做到原地更新，容易发生索引分裂影响性能。尽可能将NULL值用有意义的值代替，也能避免SQL语句里面包含is not null的判断

  - text类型

- 由于text字段储存大量数据，表容量会很早涨上去，影响其他字段的查询性能。建议抽取出来放在子表里，用业务主键关联。

- 索引优化

- - 索引分类

- 普通索引：最基本的索引。

- 组合索引：多个字段上建立的索引，能够加速复合查询条件的检索。

- 唯一索引：与普通索引类似，但索引列的值必须唯一，允许有空值。

- 组合唯一索引：列值的组合必须唯一。

- 主键索引：特殊的唯一索引，用于唯一标识数据表中的某一条记录，不允许有空值，一般用primary key约束。

- 全文索引：用于海量文本的查询，MySQL5.6之后的InnoDB和MyISAM均支持全文索引。由于查询精度以及扩展性不佳，更多的企业选择Elasticsearch。

- - 优化原则

- 分页查询很重要，如果查询数据量超过30%，MYSQL不会使用索引。

- 单表索引数不超过5个、单个索引字段数不超过5个。

- 字符串可使用前缀索引，前缀长度控制在5-8个字符。

- 字段唯一性太低，增加索引没有意义，如：是否删除、性别。

- 合理使用覆盖索引，如下所示：

- ```sql
  select login_name, nick_name from member where login_name = ?
  ```

- login_name, nick_name两个字段建立组合索引，比login_name简单索引要更快。

- SQL优化

- - 分批处理
  - 不做列运算
  - 避免Select *
  - 操作符<>优化
  - OR优化
  - IN优化
  - LIKE优化
  - JOIN优化
  - LIMIT优化

#### 配置优化

**数据库参数优化**

**①调整**

实例整体（高级优化，扩展）：

```ini
thread_concurrency：# 并发线程数量个数
sort_buffer_size：# 排序缓存
read_buffer_size：# 顺序读取缓存
read_rnd_buffer_size：# 随机读取缓存
key_buffer_size：# 索引缓存
thread_cache_size：# (1G—>8, 2G—>16, 3G—>32, >3G—>64)
```

**②连接层（基础优化）**

设置合理的连接客户和连接方式：

```ini
max_connections           # 最大连接数，看交易笔数设置    
max_connect_errors        # 最大错误连接数，能大则大
connect_timeout           # 连接超时
max_user_connections      # 最大用户连接数
skip-name-resolve         # 跳过域名解析
wait_timeout              # 等待超时
back_log                  # 可以在堆栈中的连接数量
```



**③SQL 层（基础优化）**

**query_cache_size：** 查询缓存 >>> OLAP 类型数据库，需要重点加大此内存缓存，但是一般不会超过 GB。

对于经常被修改的数据，缓存会马上失效。我们可以使用内存数据库（redis、memecache），替代它的功能。

**存储引擎层优化**

innodb 基础优化参数：

```ini
default-storage-engine
innodb_buffer_pool_size       # 没有固定大小，50%测试值，看看情况再微调。但是尽量设置不要超过物理内存70%
innodb_file_per_table=(1,0)
innodb_flush_log_at_trx_commit=(0,1,2) # 1是最安全的，0是性能最高，2折中
binlog_sync
Innodb_flush_method=(O_DIRECT, fdatasync)
innodb_log_buffer_size        # 100M以下
innodb_log_file_size          # 100M 以下
innodb_log_files_in_group     # 5个成员以下,一般2-3个够用（iblogfile0-N）
innodb_max_dirty_pages_pct   # 达到百分之75的时候刷写 内存脏页到磁盘。
log_bin
max_binlog_cache_size         # 可以不设置
max_binlog_size               # 可以不设置
innodb_additional_mem_pool_size    #小于2G内存的机器，推荐值是20M。32G内存以上100M
```

参考：https://mp.weixin.qq.com/s/ZBgv-s-_ojWMiTZYTVx-iQ



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