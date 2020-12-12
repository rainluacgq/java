### 部署msql主从

#### 概念

主从复制是指将主数据库的DDL(Data Definition Language,如创建表)和DML（Data Manipulation Language，数据库操作语言）操作通过二进制日志传到从数据库上，然后在从数据库上对这些日志进行重新执行，从而使从数据库和主数据库的数据保持一致。

#### 原理

- MySql主库在事务提交时会把数据变更作为事件记录在二进制日志Binlog中；
- 主库推送二进制日志文件Binlog中的事件到从库的中继日志Relay Log中，之后从库根据中继日志重做数据变更操作，通过逻辑复制来达到主库和从库的数据一致性；
- MySql通过三个线程来完成主从库间的数据复制，其中Binlog Dump线程跑在主库上，I/O线程和SQL线程跑着从库上；
- 当在从库上启动复制时，首先创建I/O线程连接主库，主库随后创建Binlog Dump线程读取数据库事件并发送给I/O线程，I/O线程获取到事件数据后更新到从库的中继日志Relay Log中去，之后从库上的SQL线程读取中继日志Relay Log中更新的数据库事件并应用，如下图所示。
- ![img](https://macrozheng.github.io/mall-learning/images/mysql_master_slave_06.png)

**知识扩展**

MySQL的日志系统包含：general query log、slow query log、error log（记录MySQL Server启动时、运行中、停止时的错误信息）、binary log(记录MySQL Server运行过程中的数据变更的逻辑日志)、relay log（记录从库IO线程从主库获取的主库数据变更日志）、DDL log(记录DDL语句执行时的元数据变更信息。5.7中只支持写入到文件，8.0中支持写入到innodb_ddl_log表中，注意，ddl log与online ddl的alter log不同，不要搞混了)，其中，在MySQL 5.7中，只有general query log、slow query log支持写入到表中（也支持写入到文件中），其他日志类型在MySQL 5.7版本中只支持写入到文件中，所以，下文中对于日志系统表主要介绍 general query log、slow query log表。

innodb事务日志：innodb事务日志包括redo log和undo log。redo log是重做日志，提供前滚操作，undo log是回滚日志，提供回滚操作。

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

log-bin=test-mysql-slave1-bin  

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

relay_log=test-mysql-relay-bin  

## log_slave_updates表示slave将复制事件写进自己的二进制日志

log_slave_updates=1  

## slave设置为只读（具有super权限的用户除外）

read_only=1  
```



1.show master status

![image-20200429162907245](https://github.com/rainluacgq/java/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93/pic/image-20200429162907245.png)



2.修改从源

```bash
change master to master_host='192.168.0.1', master_user='slave', master_password='123456', master_port=3306, master_log_file='test-mysql-bin.000001', master_log_pos=154, master_connect_retry=30;  
```



检查是否开启log-bin，ON表示开启

![image-20200429161231214](https://github.com/rainluacgq/java/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93/pic/image-20200429161231214.png)



3.开启同步

```
start slave;
```

![image-20200429163938073](https://github.com/rainluacgq/java/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93/pic/image-20200429163938073.png)

##### 常见问题总结：

##### 1.uuid相同导致的问题

![image-20200429142422962](https://github.com/rainluacgq/java/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93/pic/image-20200429142422962.png)



###### 2.my.cnf 权限导致的问题

![image-20200429142714642](https://github.com/rainluacgq/java/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93/pic/image-20200429142714642.png)

####   3.master position不正确导致的问题

![image-20200429163310172](https://github.com/rainluacgq/java/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93/pic/image-20200429163310172.png)
