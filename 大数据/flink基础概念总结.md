### Flink基础概念总结

### 一、概念

Apache Flink 是一个解决实时数据处理的计算框架，但不是数据仓库的服务，其可对有限数据流和无限数据流进行有状态计算，并可部署在各种集群环境，对各种大小的数据规模进行快速计算。

![image-20200729210058970](https://github.com/rainluacgq/java/blob/master/大数据/pic/image-20200729210058970.png)

Flink 是一套集高吞吐、低延迟、有状态三者于一身的分布式流式数据处理框架。

###  特点

####  1.事件驱动

传统的事务处理应用的点击流 Events 可以通过 Application 写入 Transaction DB（数据库），同时也可以通过 Application 从 Transaction DB 将数据读出，并进行处理，当处理结果达到一个预警值就会触发一个 Action 动作。

![image-20200729210328069](https://github.com/rainluacgq/java/blob/master/大数据/pic/image-20200729210328069.png)

而事件驱动的应用处理采集的数据 Events 可以不断的放入消息队列，Flink 应用会不断 ingest（消费）消息队列中的数据，Flink 应用内部维护着一段时间的数据（state），隔一段时间会将数据持久化存储（Persistent sstorage），防止 Flink 应用死掉。Flink 应用每接受一条数据，就会处理一条数据，处理之后就会触发（trigger）一个动作（Action），同时也可以将处理结果写入外部消息队列中，其他 Flink 应用再消费。并且可以通过 checkpoint 机制保证一致性，避免意外情况。

![image-20200729210351755](https://github.com/rainluacgq/java/blob/master/大数据/pic/image-20200729210351755.png)

####  2.低时延

![image-20200729211140694](https://github.com/rainluacgq/java/blob/master/大数据/pic/image-20200729211140694.png)

Flink 引入了 Buffer Pool 和 Buffer 块的概念。在大流量时，由于 Buffer 区很快就会被写满，紧接着会通知 Netty 尽可能地发送，因此不会看到太多的延迟。但在低流量时，可能几秒钟才会有一条数据，这就意味着 Buffer pool 有很长时间没有被强制写满，因此为了保证下游系统尽可能尽快得到上游的消息，就需要有一个强制的刷新或往下游推送的触发器机制。

Flink 本身则具备这样的一个机制，它可以尽可能地保证 Buffer 还没有写满时，就可提前去通知 Netty 服务器，尽快把当前 Buffer 块里面的数据发送下去，并可以通过 BufferTimeout 的参数设置，控制 Flink 在低流量时的系统最大延迟。

####  3.有状态

![image-20200729211300807](https://github.com/rainluacgq/java/blob/master/大数据/pic/image-20200729211300807.png)

由于 Flink 是一个实时计算的框架，因此 Flink 的状态实际上是最核心的技术资产，涉及到了频繁的写入与读取，并需要用很快的存储方案存储该状态。Flink 提供了三种状态的存储模式，分别是内存模式、文件模式和 Rocks DB 的模式。

- 内存模式：使用这种方式，Flink 会将状态维护在 Java 堆上。众所周知，内存的访问读写速度最快；其缺点也显而易见，单台机器的内存空间有限，不适合存储大数据量的状态信息。一般在本地开发调试时或者状态非常小的应用场景下使用内存这种方式。
- 文件模式：当选择使用文件系统作为后端时，正在计算的数据会被暂存在 TaskManager 的内存中。Checkpoint 时，此后端会将状态快照写入配置的文件系统中，同时会在 JobManager 的内存中或者在 Zookeeper 中（高可用情况）存储极少的元数据。文件系统后端适用于处理大状态，长窗口，或大键值状态的任务。
- RocksDB：RocksDB 是一种嵌入式键值数据库。使用 RocksDB 作为后端时，Flink 会将实时处理中的数据使用 RocksDB 存储在本地磁盘上。Checkpoint 时，整个 RocksDB 数据库会被存储到配置的文件系统中，同时 Flink 会将极少的元数据存储在 JobManager 的内存中，或者在 Zookeeper 中（高可用情况）。RocksDB 支持增量 Checkpoint，即只对修改的数据做备份，因此非常适合超大状态的场景。

####  4.分层API

![image-20200729211724567](https://github.com/rainluacgq/java/blob/master/大数据/pic/image-20200729211724567.png)



最底层级的抽象仅仅提供了有状态流，它将通过过程函数（Process Function） 被嵌入到 DataStream API 中。底层过程函数（ Process Function） 与 DataStream API 相集成，使其可以对某些特定的操作进行底层的抽象，它允许用户可以自由地处理来自一个或多个数据流的事件，并使用一致的容错的状态。除此之外，用户可以注册事件时间并处理时间回调，从而使程序可以处理复杂的计算。

实际上，大多数应用并不需要上述的底层抽象，而是针对核心 API（ Core APIs） 进行编程，比如 DataStream API（ 有界或无界流数据） 以及 DataSet API（有界数据集）。这些 API 为数据处理提供了通用的构建模块， 比如由用户定义的多种形式的转换（ transformations），连接（ joins），聚合（ aggregations），窗口操作（ windows） 等等。DataSet API 为有界数据集提供了额外的支持， 例如循环与迭代。这些 API 处理的数据类型以类（ classes）的形式由各自的编程语言所表示。

Table API 是以表为中心的声明式编程，其中表可能会动态变化（在表达流数据时）。Table API 遵循（ 扩展的）关系模型：表有二维数据结构（ schema）（ 类似于关系数据库中的表），同时 API 提供可比较的操作，例如 select、project、join、group-by、aggregate 等。Table API 程序声明式地定义了什么逻辑操作应该执行，而不是准确地确定这些操作代码的看上去如何。

尽管 Table API 可以通过多种类型的用户自定义函数（ UDF）进行扩展，其仍不如核心 API 更具表达能力，但是使用起来却更加简洁（代码量更少）。除此之外， Table API 程序在执行之前会经过内置优化器进行优化。

你可以在表与 DataStream/DataSet 之间无缝切换，以允许程序将 Table API 与DataStream 以及 DataSet 混合使用。

Flink 提供的最高层级的抽象是 SQL 。这一层抽象在语法与表达能力上与Table API 类似，但是是以 SQL 查询表达式的形式表现程序。SQL 抽象与 Table API 交互密切，同时 SQL 查询可以直接在 Table API 定义的表上执行。

目前 Flink 作为批处理还不是主流，不如 Spark 成熟，所以 DataSet 使用的并不是很多。Flink Table API 和 Flink SQL 也并不完善，大多都由各大厂商自己定制。所以我们主要学习 DataStream API 的使用。实际上 Flink 作为最接近 Google DataFlow 模型的实现，是流批统一的观点，所以基本上使用 DataStream 就可以了。

参考：https://mp.weixin.qq.com/s/TZjOKBkJ-6Q7iFrMjNPIdA