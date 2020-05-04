## kafka学习笔记

### 概念

Kafka是最初由Linkedin公司开发，是一个分布式、支持分区的（partition）、多副本的（replica），基于zookeeper协调的分布式消息系统，它的最大的特性就是可以实时的处理大量数据以满足各种需求场景：比如基于hadoop的批处理系统、低延迟的实时系统、storm/Spark流式处理引擎，web/nginx日志、访问日志，消息服务等等，用scala语言编写，Linkedin于2010年贡献给了Apache基金会并成为顶级开源项目。

### 1.1 特性

高吞吐量、低延迟：kafka每秒可以处理几十万条消息，它的延迟最低只有几毫秒，每个topic可以分多个partition, consumer group 对partition进行consume操作。

持久性、可靠性：消息被持久化到本地磁盘，并且支持数据备份防止数据丢失

容错性 ：允许集群中节点失败（若副本数量为n,则允许n-1个节点失败）

高并发：支持数千个客户端同时读写



####  1.2 设计思想

![image-20200504122820087](https://github.com/rainluacgq/java/blob/master/%E6%B6%88%E6%81%AF%E9%98%9F%E5%88%97/pic/image-20200504122820087.png)

1） Producer ： 消息生产者，就是向 kafka broker 发消息的客户端；
2） Consumer ： 消息消费者，向 kafka broker 取消息的客户端；
3） Consumer Group （CG）： 消费者组，由多个 consumer 组成。 消费者组内每个消费者负责消费不同分区的数据，一个分区只能由一个组内消费者消费；消费者组之间互不影响。 所有的消费者都属于某个消费者组，即消费者组是逻辑上的一个订阅者。
4） Broker ： 一台 kafka 服务器就是一个 broker。一个集群由多个 broker 组成。一个 broker可以容纳多个 topic。
5） Topic ： 可以理解为一个队列， 生产者和消费者面向的都是一个 topic；
6） Partition： 为了实现扩展性，一个非常大的 topic 可以分布到多个 broker（即服务器）上，一个 topic 可以分为多个 partition，每个 partition 是一个有序的队列；
7） Replica： 副本，为保证集群中的某个节点发生故障时， 该节点上的 partition 数据不丢失，且 kafka 仍然能够继续工作， kafka 提供了副本机制，一个 topic 的每个分区都有若干个副本，一个 leader 和若干个 follower。
8） leader： 每个分区多个副本的“主”，生产者发送数据的对象，以及消费者消费数据的对象都是 leader。
9） follower： 每个分区多个副本中的“从”，实时从 leader 中同步数据，保持和 leader 数据的同步。 leader 发生故障时，某个 follower 会成为新的 follower。  

#### 数据存储设计

#### 1.1 partttion数据文件

message包括3个属性：offset、messageSize，data

1.2  数据文件分段

1.3 数据文件索引（分段索引、稀疏存储）

![image-20200427210852708](https://github.com/rainluacgq/java/blob/master/%E6%B6%88%E6%81%AF%E9%98%9F%E5%88%97/pic/image-20200427210852708.png)



![image-20200427203955968](https://github.com/rainluacgq/java/blob/master/%E6%B6%88%E6%81%AF%E9%98%9F%E5%88%97/pic/image-20200427203955968.png)





#### 生产者设计

负载均衡（partition 会均衡分布到不同 broker 上）  

批量发送   

以一次请求的方式发送了批量的消息给 broker，从而大大减少broker 存储消息的 IO 操作次数。但也一定程度上影响了消息的实时性，相当于以时延代价，换取更好的吞吐量  

压缩（gzip或者snappy）





重复消费 

先提交 后处理offset

消息漏消费

分区分配

range roundroubin

分布式 顺序读  0拷贝 

##### Kafka 高效读写数据

1）顺序写磁盘
Kafka 的 producer 生产数据，要写入到 log 文件中，写的过程是一直追加到文件末端，为顺序写。 
2）零复制技术

**1、mmap**

减少拷贝次数的一种方法是调用mmap()来代替read调用：

```c
buf = mmap(diskfd, len);
write(sockfd, buf, len);
```

**2、sendfile**

使用sendfile不仅减少了数据拷贝的次数，还减少了上下文切换，数据传送始终只发生在kernel space。下图为使用DMA的sendfile零拷贝技术图。

![image-20200504152811317](https://github.com/rainluacgq/java/blob/master/%E6%B6%88%E6%81%AF%E9%98%9F%E5%88%97/pic/image-20200504152811317.png)

使用sendfile不仅减少了数据拷贝的次数，还减少了上下文切换，数据传送始终只发生在kernel space。下图为使用DMA的sendfile零拷贝技术图。

##### 实战

1.启动zk

在Kafka根目录下使用cmd执行下面这条命令，启动ZK：

```
bin\windows\zookeeper-server-start.bat config\zookeeper.properties
```

在Linux下，可以使用后台进程的方式启动ZK：

```
bin/zookeeper-server-start.sh -daemon config/zookeeper.properties
```

减少拷贝次数的一种方法是调用mmap()来代替read调用：

### 启动Kafka

执行下面这条命令启动Kafka：

```bash
bin\windows\kafka-server-start.bat config\server.propertie
```

Linux对应命令：

```shell
bin/kafka-server-start.sh config/server.properties
```

###### 创建Topic

执行下面这条命令创建一个Topic

```
bin\windows\kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
```

这条命令的意思是，创建一个Topic到ZK（指定ZK的地址），副本个数为1，分区数为1，Topic的名称为test。

Linux对应的命令为:

```
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
```

replication 大于broker时：

```scala
Error while executing topic command : Replication factor: 2 larger than available brokers: 1.
[2020-05-04 10:49:07,667] ERROR org.apache.kafka.common.errors.InvalidReplicationFactorException: Replication factor: 2 larger than available brokers: 1.
 (kafka.admin.TopicCommand$)
```

创建好后我们可以查看Kafka里的Topic列表：

```
bin\windows\kafka-topics.bat --list --zookeeper localhost:2181
```

Linux对应的命令为：

```
bin/kafka-topics.sh --list --zookeeper localhost:2181
```

查看test Topic的具体信息：

```
bin\windows\kafka-topics.bat --describe --zookeeper localhost:2181 --topic test
```

Linux对应的命令为：

```
bin/kafka-topics.sh --describe --zookeeper localhost:2181 --topic test
```

###### 生产消息和消费消息

**启动Producers**

```
bin\windows\kafka-console-producer.bat --broker-list localhost:9092 --topic test
```

9092为生产者的默认端口号。这里启动了生产者，准备往test Topic里发送数据。

Linux下对应的命令为：

```
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test
```

**启动Consumers**

接着启动一个消费者用于消费生产者生产的数据，新建一个cmd窗口，输入下面这条命令：

```
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic test --from-beginning
```

`from-beginning`表示从头开始读取数据。

Linux下对应的命令为：

```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning
```

![image-20200504113059980](https://github.com/rainluacgq/java/blob/master/%E6%B6%88%E6%81%AF%E9%98%9F%E5%88%97/pic/image-20200504113059980.png)

由于生产者生产的消息会不断追加到 log 文件末尾， 为防止 log 文件过大导致数据定位效率低下， Kafka 采取了分片和索引机制，将每个 partition 分为多个 segment。 每个 segment对应两个文件——“.index”文件和“.log”文件。  

![image-20200504113146775](https://github.com/rainluacgq/java/blob/master/%E6%B6%88%E6%81%AF%E9%98%9F%E5%88%97/pic/image-20200504113146775.png)



kafka server.properties  配置参考

```ini
#broker 的全局唯一编号，不能重复
broker.id=0
#删除 topic 功能使能
delete.topic.enable=true
#处理网络请求的线程数量
num.network.threads=3
#用来处理磁盘 IO 的现成数量
num.io.threads=8
#发送套接字的缓冲区大小
socket.send.buffer.bytes=102400
#接收套接字的缓冲区大小
socket.receive.buffer.bytes=102400
#请求套接字的缓冲区大小
socket.request.max.bytes=104857600
#kafka 运行日志存放的路径
log.dirs=/opt/module/kafka/logs
#topic 在当前 broker 上的分区个数
num.partitions=1
#用来恢复和清理 data 下数据的线程数量
num.recovery.threads.per.data.dir=1
#segment 文件保留的最长时间，超时将被删除
log.retention.hours=168
#配置连接 Zookeeper 集群地址
zookeeper.connect=hadoop102:2181,hadoop103:2181,hadoop104:2181  
```

