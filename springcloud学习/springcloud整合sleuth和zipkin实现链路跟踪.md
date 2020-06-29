###  SpringCloud整合zipkin、kafka、ES实现链路监控

#### 1.概念一览

随着我们的系统越来越庞大，各个服务间的调用关系也变得越来越复杂。当客户端发起一个请求时，这个请求经过多个服务后，最终返回了结果，经过的每一个服务都有可能发生延迟或错误，从而导致请求失败。这时候我们就需要请求链路跟踪工具来帮助我们，理清请求调用的服务链路，解决问题。

分布式系统的服务跟踪主要包括以下两个方面

- 为了实现请求跟踪，当请求发送到分布式系统的入口端口时，只需要服务跟踪框架为该请求创建一个唯一的跟踪标识，同时在分布式内部流转的时候，框架始终保持保持该唯一标识，直到返回给请求方为止，这个唯一标识就是TraceID

- 为了统计各处理单元的时间延迟，当请求到达各个服务组件时，也通过一个唯一标识来标记他的开始、具体过程以及结束，即SPAN ID。

  ![image-20200629193126286](https://github.com/rainluacgq/java/blob/master/springcloud学习/pic/image-20200629193126286.png)



#### 2.环境搭建

下载zipkin Server，也可通过springboot直接集成zipkin server，但是zipkin官方推荐使用jar包的形式。

https://repo1.maven.org/maven2/io/zipkin/zipkin-server/2.21.4/zipkin-server-2.21.4-exec.jar

运行zipkin Server

java -jar zipkin-server-2.12.9-exec.jar

若需要启动时将数据存储到ES中，则需要执行如下操作：

java -DKAFKA_ZOOKEEPER=localhost:2181 -DSTORAGE_TYPE=elasticsearch  -DES_HOSTS=http://localhost:9200  -jar  zipkin-server-2.12.9-exec.jar

![image-20200629194055603](C:\Users\caiguoqing\AppData\Roaming\Typora\typora-user-images\image-20200629194055603.png)

- Zipkin页面访问地址：[http://localhost:9411](http://localhost:9411/)

  ![image-20200629194124444](https://github.com/rainluacgq/java/blob/master/springcloud学习/pic/image-20200629194124444.png)

### kafka环境搭建

启动zookeeper

docker run -d --name zookeeper -p 2181:2181 -v /etc/localtime:/etc/localtime zookeeper

启动kafka

docker run -d --name kafka -p 9092:9092 --link zookeeper --env KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 --env KAFKA_ADVERTISED_HOST_NAME=localhost --env KAFKA_ADVERTISED_PORT=9092 -v /etc/localtime:/etc/localtime wurstmeister/kafka:latest

环境搭建参考：https://www.jianshu.com/p/e8c29cba9fae

### 3.集成spring sleuth

依赖

```xml
<!--使用分布式请求链路追踪时添加-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-sleuth-zipkin-stream</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream-binder-kafka</artifactId>
</dependency>
```

配置

如果需要整合sleuth、zipkin、kafka，可如下配置

```yml
cloud:
  stream:
    kafka:
      binder:
        brokers: kafka_ip:9092
        zkNodes: zk_ip:2181
zipkin:
  storage:
    type: elasticsearch
    elasticsearch:
      cluster: elasticsearch
      hosts: http://es_ip:9200
      index: zipkin
      index-shards: 5
      index-replicas: 1
```

如果直接存储在zipkin中，可如下配置

```yaml
zipkin:
	base-url: http://zipkin_server:9411
```

搭建成功可实现如下效果：

![image-20200629194629314](https://github.com/rainluacgq/java/blob/master/springcloud学习/pic/image-20200629194629314.png)