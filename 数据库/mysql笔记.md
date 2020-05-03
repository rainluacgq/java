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

### 部署msql主从



#### 概念

主从复制是指将主数据库的DDL(Data Definition Language,如创建表)和DML（Data Manipulation Language，数据库操作语言）操作通过二进制日志传到从数据库上，然后在从数据库上对这些日志进行重新执行，从而使从数据库和主数据库的数据保持一致。



## [主从复制的原理]

- MySql主库在事务提交时会把数据变更作为事件记录在二进制日志Binlog中；
- 主库推送二进制日志文件Binlog中的事件到从库的中继日志Relay Log中，之后从库根据中继日志重做数据变更操作，通过逻辑复制来达到主库和从库的数据一致性；
- MySql通过三个线程来完成主从库间的数据复制，其中Binlog Dump线程跑在主库上，I/O线程和SQL线程跑着从库上；
- 当在从库上启动复制时，首先创建I/O线程连接主库，主库随后创建Binlog Dump线程读取数据库事件并发送给I/O线程，I/O线程获取到事件数据后更新到从库的中继日志Relay Log中去，之后从库上的SQL线程读取中继日志Relay Log中更新的数据库事件并应用，如下图所示。
- ![img](https://macrozheng.github.io/mall-learning/images/mysql_master_slave_06.png)

##### 1.mysql 主服务器配置

1.新建my.cnf

主数据库配置

```
[mysqld]

#设置server_id，同一局域网中需要唯一

server_id=101 

#指定不需要同步的数据库名称

binlog-ignore-db=mysql  

#开启二进制日志功能

log-bin=mall-mysql-bin  

#设置二进制日志使用内存大小（事务）

binlog_cache_size=1M  

#设置使用的二进制日志格式（mixed,statement,row）

binlog_format=mixed  

#二进制日志过期清理时间。默认值为0，表示不自动清理。

expire_logs_days=7  

#跳过主从复制中遇到的所有错误或指定类型的错误，避免slave端复制中断。

#如：1062错误是指一些主键重复，1032错误是因为主从数据库数据不一致

slave_skip_errors=1062  
```



##### 2 从服务器配置

mysql 从数据库配置

```
[mysqld]
##server_id，同一局域网中需要唯一
server_id=105

## 指定不需要同步的数据库名称

binlog-ignore-db=mysql  

## 开启二进制日志功能，以备Slave作为其它数据库实例的Master时使用

log-bin=hik-mysql-slave1-bin  

## 设置二进制日志使用内存大小（事务）

binlog_cache_size=1M  

## 设置使用的二进制日志格式（mixed,statement,row）

binlog_format=mixed  

## 二进制日志过期清理时间。默认值为0，表示不自动清理。

expire_logs_days=7  

## 跳过主从复制中遇到的所有错误或指定类型的错误，避免slave端复制中断。

## 如：1062错误是指一些主键重复，1032错误是因为主从数据库数据不一致

slave_skip_errors=1062  

## relay_log配置中继日志

relay_log=hik-mysql-relay-bin  

## log_slave_updates表示slave将复制事件写进自己的二进制日志

log_slave_updates=1  

## slave设置为只读（具有super权限的用户除外）

read_only=1  
```



1.show master status

![image-20200429162907245](C:\Users\caiguoqing\AppData\Roaming\Typora\typora-user-images\image-20200429162907245.png)



2.修改从源

```bash
change master to master_host='10.7.32.62', master_user='slave', master_password='123456', master_port=3306, master_log_file='hik-mysql-bin.000001', master_log_pos=154, master_connect_retry=30;  
```



检查是否开启log-bin，ON表示开启

![image-20200429161231214](C:\Users\caiguoqing\AppData\Roaming\Typora\typora-user-images\image-20200429161231214.png)



3.开启同步

```
start slave;
```

![image-20200429163938073](C:\Users\caiguoqing\AppData\Roaming\Typora\typora-user-images\image-20200429163938073.png)

##### 常见问题总结：

##### 1.uuid相同导致的问题

![image-20200429142422962](C:\Users\caiguoqing\AppData\Roaming\Typora\typora-user-images\image-20200429142422962.png)



###### 2.my.cnf 权限导致的问题

![image-20200429142714642](C:\Users\caiguoqing\AppData\Roaming\Typora\typora-user-images\image-20200429142714642.png)

####   3.master position不正确导致的问题

![image-20200429163310172](C:\Users\caiguoqing\AppData\Roaming\Typora\typora-user-images\image-20200429163310172.png)