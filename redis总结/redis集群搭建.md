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

![image-20200617202446030](https://github.com/rainluacgq/java/blob/master/redis总结/pic/image-20200617202446030.png)



从resis运行在6381端口



![image-20200617202550342](https://github.com/rainluacgq/java/blob/master/redis总结/pic/image-20200617202550342.png)



修改sentinel的配置文件如下所示：

```
# Note: master name should not include special characters or spaces.
# The valid charset is A-z 0-9 and the three characters ".-_".
sentinel myid 5670007acf83027393f8bd5dea52a07524600dbe
sentinel monitor mymaster 127.0.0.1 6380 1
```

如果哨兵启动成功可以看到如下日志：

![image-20200618195550340](https://github.com/rainluacgq/java/blob/master/redis总结/pic/image-20200618195550340.png)

当关闭掉主数据时

![image-20200618201352987](https://github.com/rainluacgq/java/blob/master/redis总结/pic/image-20200618201352987.png)

可见原来的从库6382已经变成主库，其从库是6381

![image-20200618201418714](https://github.com/rainluacgq/java/blob/master/redis总结/pic/image-20200618201418714.png)

可能遇到的问题

当关闭主库时，主从无法切换并报错误

![image-20200618201607368](https://github.com/rainluacgq/java/blob/master/redis总结/pic/image-20200618201607368.png)

修改sentinel的配置文件，关闭保护模式：

```
# For example you may use one of the following:
# bind 127.0.0.1 192.168.1.1
protected-mode no
```



###  redis cluster集群搭建

1、Redis cluster部署条件

至少要有三个主节点才能构成集群，数据分区规则采用虚拟槽（16384个槽）方式，每个节点负责一部分数据。除了三个主节点意外还需要为他们各自配置一个slave以支持主从切换。Redis cluster采用了分片机制，当有多个节点的时候，其每一个节点都可以作为客户端的入口进行连接并写入数据。Redis cluster的分片机制让每个节点都存放了一部分数据（比如有1W个key分布在了5个cluster节点上，每个节点可能只存储了2000个key， 但是每一个节点都有一个类似index索引的东西记录了所有key的分布情况。），且每一个节点还应该有一个slave节点作为备份节点（比如用3台机器部署成Redis cluster，还应该为这三台Redis做主从部署，所以一共要6台机器），当master节点故障时slave节点会选举提升为master节点。

2、Redis cluster集群什么时候不可用

任意master挂掉且该master没有slave节点，集群将进入fail状态。如果master有slave节点，但是有半数以上master挂掉，集群也将进入fail状态。当集群fail时，所有对集群的操作都不可用，会出现clusterdown the cluster is down的错误

3、Redis cluster集群监控端口

16379（其实是Redis当前的端口+10000，因为默认是6379，所以这里就成了16379，用于集群节点相互通信）

4、Redis cluster容错与选举机制

所有master参与选举，当半数以上的master节点与故障节点通信超时将触发故障转移

**二、Redis cluster配置文件**

redis-cluster的配置信息包含在了redis.conf中，要修改的主要有6个选项（每一个节点都需要做这些配置）：


**三、正式启动redis cluster服务**

首先要让集群正常运作至少需要三个主节点（考虑主从的话还需要额外准备三个从节点）。做实验的话可以用二台机器启动不同的redis端口（比如6379、6380、6381）来模拟不同的机器。

1、每个主从节点均需要启动cluster服务，启动后查看下端口，可以看到端口后面多了cluster这样的信息

[![cluster2.png](http://www.linuxe.cn/content/uploadfile/201707/0bd61499237276.png)](http://www.linuxe.cn/content/uploadfile/201707/0bd61499237276.png)

**四、测试集群工作**

1、集群一旦搭建好了后必须使用redis-cli -c 选项以集群模式进行操作。集群模式下只有0号数据库可用，无法再通过select来切换数据库。登录后创建一些key用于测试，可以看到输出信息显示这个key是被存到了其他机器上。使用get获取key的时候也可以看到该key是被分配到了哪个节点

[![cluster3.png](http://www.linuxe.cn/content/uploadfile/201707/f8d91499239663.png)](http://www.linuxe.cn/content/uploadfile/201707/f8d91499239663.png)

2、查看redis cluster节点状态

cluster nodes命令可以看到自己和其他节点的状态，集群模式下有主节点挂掉的话可以在这里观察到切换情况；cluste info命令可以看到集群的详细状态，包括集群状态、分配槽信息

[![微信截图_20180902152300.png](http://www.linuxe.cn/content/uploadfile/201809/bde01535872992.png)](http://www.linuxe.cn/content/uploadfile/201809/bde01535872992.png)

3、手动切换redis cluster的主从关系：

redis cluster发生主从切换后，即使之前的主节点恢复了也不会变回主节点，而是作为从节点在工作，这一点和sentine模式是一样的。如果想要它变回主节点，只需要在该节点执行命令cluster failover

```
        daemonize yes #后台启动
        port 7001 #修改端口号，从7001到7006
        cluster-enabled yes #开启cluster，去掉注释
        cluster-config-file nodes.conf #自动生成
        cluster-node-timeout 15000 #节点通信时间
        appendonly yes #持久化方式
```

参考：http://www.linuxe.cn/post-375.html