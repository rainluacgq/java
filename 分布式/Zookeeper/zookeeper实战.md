### Zookeeper实战

1.安装与配置：

进入conf文件夹复制zoo_sample.cfg，命名为zoo.cfg

```ini
# The number of milliseconds of each tick
tickTime=2000
# The number of ticks that the initial 
# synchronization phase can take
initLimit=10
# The number of ticks that can pass between 
# sending a request and getting an acknowledgement
syncLimit=5
# the directory where the snapshot is stored.
# do not use /tmp for storage, /tmp here is just 
# example sakes.
dataDir=/tmp/zookeeper
# the port at which the clients will connect
clientPort=2181
# the maximum number of client connections.
# increase this if you need to handle more clients
#maxClientCnxns=60
#
# Be sure to read the maintenance section of the 
# administrator guide before turning on autopurge.
# http://zookeeper.apache.org/doc/current/zookeeperAdmin.html#sc_maintenance
#
# The number of snapshots to retain in dataDir
#autopurge.snapRetainCount=3
# Purge task interval in hours
# Set to "0" to disable auto purge feature
#autopurge.purgeInterval=1
## Metrics Providers
#
# https://prometheus.io Metrics Exporter
#metricsProvider.className=org.apache.zookeeper.metrics.prometheus.PrometheusMetricsProvider
#metricsProvider.httpPort=7000
#metricsProvider.exportJvmInfo=true
```

启动zk：

linux环境：

```bash
bin/zkServer.sh start
```

windows

```
.\bin\zkServer.cmd
```

通过客户端连接zk：

```
.\bin\zkCli.cmd
```

客户端基本命令：

![image-20201031171145057](C:\Users\19349\AppData\Roaming\Typora\typora-user-images\image-20201031171145057.png)

查看某节点的状态：

```bash
[zk: localhost:2181(CONNECTED) 8] stat /test
cZxid = 0xd # 创建节点时的事务 ID，由 ZooKeeper 维护。
ctime = Tue Jun 12 07:45:53 PDT 2018
mZxid = 0x1f # 当前节点携带数据最后一次修改的事务 ID。
mtime = Tue Jun 12 07:52:53 PDT 2018
pZxid = 0x21 # 子节点列表最后一次修改的事务 ID。
cversion = 1 # 节点版本号，当节点的子节点列表发生变化时，版本变更。
dataVersion = 2 # 数据版本号，当节点携带数据发生变化时，版本变更。
aclVersion = 0
ephemeralOwner = 0x0 # 此数据值不是 0x0 时，代表是临时节点
dataLength = 3 # 节点携带数据长度
numChildren = 1 # 子节点数量
```

### 使用Curator整合zookeeper

Curator是Netflix公司开源的一套zookeeper客户端框架，解决了很多Zookeeper客户端非常底层的细节开发工作，包括连接重连、反复注册Watcher和NodeExistsException异常等等。Patrixck Hunt（Zookeeper）以一句“Guava is to Java that Curator to Zookeeper”给Curator予高度评价。

注意可能会有zookeeper和curator的兼容性问题：

```xml
<?xml version="1.0"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com</groupId>
  <artifactId>demo</artifactId>
  <version>y</version>
  <name>demo</name>
  <url>http://maven.apache.org</url>
  <dependencies>
    <dependency>
      <groupId>org.apache.curator</groupId>
      <artifactId>curator-framework</artifactId>
      <version>5.1.0</version>
    </dependency>
    <dependency>
      <groupId>org.apache.curator</groupId>
      <artifactId>curator-recipes</artifactId>
      <version>5.1.0</version>
    </dependency>
    <!--日志模块 统一使用sl4fj -->
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-api</artifactId>
      <version>1.7.21</version>
    </dependency>
    <dependency>
      <groupId>org.apache.logging.log4j</groupId>
      <artifactId>log4j-slf4j-impl</artifactId>
      <version>2.6.2</version>
    </dependency>
    <dependency>
      <groupId>org.apache.logging.log4j</groupId>
      <artifactId>log4j-core</artifactId>
      <version>2.6.2</version>
    </dependency>
  </dependencies>
</project>
```

连接zookeeper：

```java
private static final int BASE_SLEEP_TIME = 1000;
private static final int MAX_RETRIES = 3;

public  static CuratorFramework connectZk() {
    // Retry strategy. Retry 3 times, and will increase the sleep time between retries.
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
    CuratorFramework zkClient = CuratorFrameworkFactory.builder()
            // the server to connect to (can be a server list)
            .connectString("127.0.0.1:2181")
            .retryPolicy(retryPolicy)
            .build();
    zkClient.start();
    return zkClient;
}
```

创建节点：

我们在 [ZooKeeper常见概念解读](https://github.com/Snailclimb/JavaGuide/blob/master/docs/system-design/distributed-system/zookeeper/zookeeper-intro.md) 中介绍到，我们通常是将 znode 分为 4 大类：

- **持久（PERSISTENT）节点** ：一旦创建就一直存在即使 ZooKeeper 集群宕机，直到将其删除。
- **临时（EPHEMERAL）节点** ：临时节点的生命周期是与 **客户端会话（session）** 绑定的，**会话消失则节点消失** 。并且，临时节点 **只能做叶子节点** ，不能创建子节点。
- **持久顺序（PERSISTENT_SEQUENTIAL）节点** ：除了具有持久（PERSISTENT）节点的特性之外， 子节点的名称还具有顺序性。比如 `/node1/app0000000001` 、`/node1/app0000000002` 。
- **临时顺序（EPHEMERAL_SEQUENTIAL）节点** ：除了具备临时（EPHEMERAL）节点的特性之外，子节点的名称还具有顺序性。

你在使用的ZooKeeper 的时候，会发现 `CreateMode` 类中实际有 7种 znode 类型 ，但是用的最多的还是上面介绍的 4 种。你可以通过下面两种方式创建持久化的节点。

```java
//注意:下面的代码会报错，下文说了具体原因
zkClient.create().forPath("/node1/00001");
zkClient.create().withMode(CreateMode.PERSISTENT).forPath("/node1/00002");
```

但是，你运行上面的代码会报错，这是因为的父节点`node1`还未创建。

你可以先创建父节点 `node1` ，然后再执行上面的代码就不会报错了。

```java
zkClient.create().forPath("/node1");
```

更推荐的方式是通过下面这行代码， **`creatingParentsIfNeeded()` 可以保证父节点不存在的时候自动创建父节点，这是非常有用的。**

```java
zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/node1/00001");
```

**b.创建临时节点**

```
zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/node1/00001");
```

**c.创建节点并指定数据内容**

```
zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/node1/00001","java".getBytes());
zkClient.getData().forPath("/node1/00001");//获取节点的数据内容，获取到的是 byte数组
```

**d.检测节点是否创建成功**

```
zkClient.checkExists().forPath("/node1/00001");//不为null的话，说明节点创建成功
```

### 分布式锁：

```
InterProcessLock lock = new InterProcessSemaphoreMutex(client, lockPath);
```