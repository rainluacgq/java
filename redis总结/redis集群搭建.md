Redis集群搭建

### 一、主从模式 + 哨兵

修改port口

```ini
# Accept connections on the specified port, default is 6379 (IANA #815344).
# If port 0 is specified Redis will not listen on a TCP socket.
port 6380cn
```

修改pid

```ini
# Specify the log file name. Also the empty string can be used to force
# Redis to log on the standard output. Note that if you use standard
# output for logging but daemonize, logs will be sent to /dev/null
logfile /var/log/redis/redis-server-6380.log
```

修改Dump.rdp文件

```
# The filename where to dump the DB
dbfilename dump-6380.rdb
```

在从数据库上还需要增加

```ini
# slaveof <masterip> <masterport>
slaveof   127.0.0.1 6380
```

分别启动主从redis

```
redis-server ./redis-master.conf

redis-server ./redis-slave.conf
```

看一下效果

主redis运行在6380端口

![image-20200617202446030](C:\Users\caiguoqing\AppData\Roaming\Typora\typora-user-images\image-20200617202446030.png)

从resis运行在6381端口



![image-20200617202550342](C:\Users\caiguoqing\AppData\Roaming\Typora\typora-user-images\image-20200617202550342.png)



修改sentinel的配置文件如下所示：

```
# Note: master name should not include special characters or spaces.
# The valid charset is A-z 0-9 and the three characters ".-_".
sentinel myid 5670007acf83027393f8bd5dea52a07524600dbe
sentinel monitor mymaster 127.0.0.1 6380 1
```

如果哨兵启动成功可以看到如下日志：

![image-20200618195550340](C:\Users\caiguoqing\AppData\Roaming\Typora\typora-user-images\image-20200618195550340.png)

当关闭掉主数据时

![image-20200618201352987](C:\Users\caiguoqing\AppData\Roaming\Typora\typora-user-images\image-20200618201352987.png)

可见原来的从库6382已经变成主库，其从库是6381

![image-20200618201418714](C:\Users\caiguoqing\AppData\Roaming\Typora\typora-user-images\image-20200618201418714.png)

可能遇到的问题

当关闭主库时，主从无法切换并报错误

![image-20200618201607368](C:\Users\caiguoqing\AppData\Roaming\Typora\typora-user-images\image-20200618201607368.png)

修改sentinel的配置文件，关闭保护模式：

```
# For example you may use one of the following:
# bind 127.0.0.1 192.168.1.1
protected-mode no
```



###  redis cluster集群搭建

```
        daemonize yes #后台启动
        port 7001 #修改端口号，从7001到7006
        cluster-enabled yes #开启cluster，去掉注释
        cluster-config-file nodes.conf #自动生成
        cluster-node-timeout 15000 #节点通信时间
        appendonly yes #持久化方式
```

分别启动6台机器