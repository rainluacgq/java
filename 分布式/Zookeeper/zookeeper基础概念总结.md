### Zookeeper学习

###  一、基础概念总结

Zookeeper 是一个分布式协调服务，可用于服务发现，分布式锁，分布式领导选举，配置管理等。Zookeeper 提供了一个类似于 Linux 文件系统的树形结构（可认为是轻量级的内存文件系统，但只适合存少量信息，完全不适合存储大量文件或者大文件），同时提供了对于每个节点的监控与通知机制。  

**数据结构**

zookeeper提供了类似Linux文件系统一样的数据结构。每一个节点对应一个Znode节点，每一个Znode节点都可以存储1MB（默认）的数据。客户端对zk的操作就是对Znode节点的操作。

![image-20200802190047317](C:\Users\19349\AppData\Roaming\Typora\typora-user-images\image-20200802190047317.png)

节点类型：

1. PERSISTENT：持久的节点。

2. EPHEMERAL： 暂时的节点。
3.  PERSISTENT_SEQUENTIAL：持久化顺序编号目录节点。
4. EPHEMERAL_SEQUENTIAL：暂时化顺序编号目录节点。  

####  监听器原理

（1）在Zookeeper的API操作中，创建main()主方法即主线程；

（2）在main线程中创建Zookeeper客户端（zkClient），这时会创建两个线程：

​     线程connet负责网络通信连接，连接服务器；

​     线程Listener负责监听；

（3）客户端通过connet线程连接服务器；

​     图中getChildren("/" , true) ，" / "表示监听的是根目录，true表示监听，不监听用false

（4）在Zookeeper的注册监听列表中将注册的监听事件添加到列表中，表示这个服务器中的/path，即根目录这个路径被客户端监听了；

（5）一旦被监听的服务器根目录下，数据或路径发生改变，Zookeeper就会将这个消息发送给Listener线程；

（6）Listener线程内部调用process方法，采取相应的措施，例如更新服务器列表等。

![img](https://img-blog.csdnimg.cn/20181129152427184.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3d4MTUyODE1OTQwOQ==,size_16,color_FFFFFF,t_70)

####  选举机制

1）半数机制：集群中半数以上机器存活，集群可用。所以 Zookeeper 适合安装奇数台服务器。
2） Zookeeper 虽然在配置文件中并没有指定 Master 和 Slave。 但是， Zookeeper 工作时，是有一个节点为 Leader，其他则为 Follower， Leader 是通过内部的选举机制临时产生的。

大致过程如下：  

- 服务器1启动，给自己投票，然后发投票信息，由于其它机器还没有启动所以它收不到反馈信息，服务器1的状态一直属于Looking(选举状态)。
- 服务器2启动，给自己投票，同时与之前启动的服务器1交换结果，由于服务器2的编号大所以服务器2胜出，但此时投票数没有大于半数，所以两个服务器的状态依然是LOOKING。
- 服务器3启动，给自己投票，同时与之前启动的服务器1,2交换信息，由于服务器3的编号最大所以服务器3胜出，此时投票数正好大于半数，所以服务器3成为领导者，服务器1,2成为小弟。
- 服务器4启动，给自己投票，同时与之前启动的服务器1,2,3交换信息，尽管服务器4的编号大，但之前服务器3已经胜出，所以服务器4只能成为小弟。
- 服务器5启动，后面的逻辑同服务器4成为小弟。



####   分布式一致性协议

Paxos

Paxos 算法解决的问题是一个分布式系统如何就某个值（决议）达成一致。一个典型的场景是，在一个分布式数据库系统中，如果各节点的初始状态一致，每个节点执行相同的操作序列，那么他们最后能得到一个一致的状态。为保证每个节点执行相同的命令序列，需要在每一条指令上执行一个“一致性算法”以保证每个节点看到的指令一致。 zookeeper 使用的 zab 算法是该算法的一个实现。 在 Paxos 算法中，有三种角色： Proposer， Acceptor， Learners  

Paxos 算法分为两个阶段。具体如下：
阶段一（准 leader 确定 ） ：
(a) Proposer 选择一个提案编号 N，然后向半数以上的 Acceptor 发送编号为 N 的 Prepare 请求。
(b) 如果一个 Acceptor 收到一个编号为 N 的 Prepare 请求，且 N 大于该 Acceptor 已经响应过的所有 Prepare 请求的编号，那么它就会将它已经接受过的编号最大的提案（如果有的话）作为响应反馈给 Proposer，同时该 Acceptor 承诺不再接受任何编号小于 N 的提案。
阶段二（leader 确认） ：
(a) 如果 Proposer 收到半数以上 Acceptor 对其发出的编号为 N 的 Prepare 请求的响应，那么它就会发送一个针对[N,V]提案的 Accept 请求给半数以上的 Acceptor。注意： V 就是收到的响应中编号最大的提案的 value，如果响应中不包含任何提案，那么 V 就由 Proposer 自己决定。
(b) 如果 Acceptor 收到一个针对编号为 N 的提案的 Accept 请求，只要该 Acceptor 没有对编号大于 N 的 Prepare 请求做出过响应，它就接受该提案  

Raft

与 Paxos 不同 Raft 强调的是易懂（Understandability）， Raft 和 Paxos 一样只要保证 n/2+1 节点正常就能够提供服务； raft 把算法流程分为三个子问题：选举（Leader election）、日志复制（Log replication）、安全性（Safety）三个子问题  

