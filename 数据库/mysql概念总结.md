### mysql 总结

####  4种事务数据隔离级别

```sql
1.读未提交 ISOLATION_READ_UNCOMMITTED
```

```sql
2.读已提交 ISOLATION_READ_COMMITTED 
```

```mssql
3.重复读 ISOLATION_REPEATABLE_READ 默认级别
```

```mysql
4.串行化 ISOLATION_SERIALIZABLE
```

READ_UNCOMMITTED:事务中的修改，即使没有提交，对其他事务也是可见的。

READ_COMMITTED：一个事务从开始到提交之前，所做的任何修改对其他事务都是不可见的，这个级别也叫不可重复读。因为执行两次同样的查询，可能会得到不一样的结果。

REPEATABLE_READ：可能会存在幻读的问题：即当某个事务读取某范围的记录时，另一事务在该范围插入新数据，当之前的事务再次读取该范围记录时，会产生幻行。InnoDB通过多版本并发控制（MVCC）解决了幻读的问题。

SERIALIZABLE：最高的隔离级别，会在读取的每一行加锁，可能导致大量的超时和锁争用的问题。非常需要确保数据一致性且可接受没有并发的情况下才采用该级别。

#### 并发一致性问题

1.脏读

2.丢失修改

3.不可重复读

4.幻读

#####  不同隔离级别可能导致的问题

| 隔离级别         | 脏读   | 不可重复读 | 幻读   |
| ---------------- | ------ | ---------- | ------ |
| READ_UNCOMMITTED | 可能   | 可能       | 可能   |
| READ_COMMITTED   | 不可能 | 可能       | 可能   |
| REPEATABLE_READ  | 不可能 | 不可能     | 可能   |
| SERIALIZABLE     | 不可能 | 不可能     | 不可能 |

### 并发导致死锁

两个或者多个事务在同一资源相互占用，并

### mysql索引

 	(1)   普通索引

​	（2）唯一索引

​    （3）主键

​	（4）组合索引（最左前缀）

指多个字段上创建的索引，只有在查询条件中使用了创建索引时的第一个字段，索引才会被使用。使用组合索引时遵循最左前缀集合

​	（4）全文索引(只有myslsm可用)

主要用来查找文本中的关键字，而不是直接与索引中的值相比较。fulltext索引跟其它索引大不相同，它更像是一个搜索引擎，而不是简单的where语句的参数匹配。fulltext索引配合match against操作使用，而不是一般的where语句加like。

###### **优点**

- 索引大大减小了服务器需要扫描的数据量

- 索引可以帮助服务器避免排序和临时表

- 索引可以将随机IO变成顺序IO

- 索引对于InnoDB（对索引支持行级锁）非常重要，因为它可以让查询锁更少的元组。在MySQL5.1和更新的版本中，InnoDB可以在服务器端过滤掉行后就释放锁，但在早期的MySQL版本中，InnoDB直到事务提交时才会解锁。对不需要的元组的加锁，会增加锁的开销，降低并发性。 InnoDB仅对需要访问的元组加锁，而索引能够减少InnoDB访问的元组数。但是只有在存储引擎层过滤掉那些不需要的数据才能达到这种目的。一旦索引不允许InnoDB那样做（即索引达不到过滤的目的），MySQL服务器只能对InnoDB返回的数据进行WHERE操作，此时，已经无法避免对那些元组加锁了。如果查询不能使用索引，MySQL会进行全表扫描，并锁住每一个元组，不管是否真正需要。

- - 关于InnoDB、索引和锁：InnoDB在二级索引上使用共享锁（读锁），但访问主键索引需要排他锁（写锁）

##### **缺点**

- 虽然索引大大提高了查询速度，同时却会降低更新表的速度，如对表进行INSERT、UPDATE和DELETE。因为更新表时，MySQL不仅要保存数据，还要保存索引文件。
- 建立索引会占用磁盘空间的索引文件。一般情况这个问题不太严重，但如果你在一个大表上创建了多种组合索引，索引文件的会膨胀很快。
- 如果某个数据列包含许多重复的内容，为它建立索引就没有太大的实际效果。
- 对于非常小的表，大部分情况下简单的全表扫描更高效；



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

