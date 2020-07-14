### SpringBoot 整合kafka

####  一、依赖添加

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<!--spring kafka版本可不用写 !-->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-json</artifactId>
</dependency>
```

需要注意这里的版本要求，可参照springboot 的官网推荐 https://spring.io/projects/spring-kafka#overview：

![image-20200710104038524](https://github.com/rainluacgq/java/blob/master/消息队列/pic/image-20200710104038524.png)



####  二、kafka配置修改

```ini
### 监听端口修改
listeners=PLAINTEXT://:9092
advertised.listeners=PLAINTEXT://host_ip:9092
### truly delete topic
delete.topic.enable=true
### 自动创建 topic
auto.create.topics.enable=true
```

####   三、集成springboot

#####   配置文件：

```yml
spring:
  # Kafka 配置项，对应 KafkaProperties 配置类
  kafka:
    bootstrap-servers: kafka_ip:9092 # 指定 Kafka Broker 地址，可以设置多个，以逗号分隔
    # Kafka Producer 配置项
    producer:
      acks: 1 # 0-不应答。1-leader 应答。all-所有 leader 和 follower 应答。
      retries: 3 # 发送失败时，重试发送的次数
      key-serializer: org.apache.kafka.common.serialization.StringSerializer # 消息的 key 的序列化
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer # 消息的 value 的序列化
    # Kafka Consumer 配置项
    consumer:
      auto-offset-reset: earliest # 设置消费者分组最初的消费进度为 earliest 。可参考博客 https://blog.csdn.net/lishuangzhe7047/article/details/74530417 理解
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring:
          json:
            trusted:
              packages: cn.iocoder.springboot.lab03.kafkademo.message
    # Kafka Consumer Listener 监听器配置
    listener:
      missing-topics-fatal: false # 消费监听接口监听的主题不存在时，默认会报错。所以通过设置为 false ，解决报错

logging:
  level:
    org:
      springframework:
        kafka: ERROR # spring-kafka INFO 日志太多了，所以我们限制只打印 ERROR 级别
      apache:
        kafka: ERROR # kafka INFO 日志太多了，所以我们限制只打印 ERROR 级别
```

#####   生产者重要参数配置：

acks
这个参数用老指定分区中必须由多少个副本收到消息，之后生产者才会认为这条消息写入是成功的。acks参数有三种类型的值（都是字符串类型）。

acks=1 默认值为1.生产者发送消息之后，只要分区的leader副本成功的写入消息，生产端就会收到来自服务端的成功响应，说明发送成功。如果消息无法写入leader副本，比如在leader副本崩溃、重新选举新的leader副本的过程中，生产者就会收到一个错误的响应，为了避免消息丢失，生产者就会选择重发消息；如果消息写入leader副本并成功响应给生产者，并且在其他follower副本拉取之前leader副本崩溃，此时消息还会丢失，因为新选举的leader副本中并没有这条对应的消息。acks设置为1，是消息可靠性和吞吐量之间的折中方案。
acks=0 生产者发送消息之后，不需要等待任何服务端的响应。如果在消息从发送到写入kafka的过程中出现异常，导致kafka并没有收到消息，此时生产者是不知道的，消息也就丢失了。akcs设置为0时，kafka可以达到最大的吞吐量。
acks=-1或acks=all 生产者在消息发送之后，需要等待isr中所有的副本都成功写入消息此案能够收到服务端的成功响应。acks设置为-1，可以达到相对最强的可靠性。但这不一定是最可靠的，因为isr中可能就只有leader副本，这样就退化成了acks=1 的情况，要获得更高的可靠性需要配置min.insync.replicas等参数的联动。

需要注意的是 acks参数是一个字符串类型，而不是一个整数类型。

##### 消费者重要的参数配置

enable-auto-commit: false

```
true-使用 kafka 默认自带的提交模式。false-使用 Spring-Kafka 的自动提交 offset 机制。建议设置为 false 使用 kafka-spring 的机制 具体设置可参考：https://juejin.im/entry/5a6e8dea518825732472710c
```

auto-offset-reset  指定位移消费

earliest 当分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，从头开始消费。 
latest 当分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，消费新产生的该分区下的数据。 
none 当该topic下所有分区中存在未提交的offset时，抛出异常。

发送自定义的消息：

```yml
   properties:
        spring.json.trusted.packages: "com.example.demo.dto"
```

解决如下问题：

```java
Caused by: java.lang.IllegalArgumentException: The class 'com.example.demo.dto.TestMessage' is not in the trusted packages: [java.util, java.lang, com.example.demo.dto.]. If you believe this class is safe to deserialize, please provide its name. If the serialization is only done by a trusted source, you can also enable trust all (*).
```

Kafka Consumer Listener设置：

```yml
listener:
  concurrency: 10 # 每个消费者监听器最大并发数，默认为 1 。可以通过设置 n ，这样对于每个监听器就会使用 n 个线程消费消息，提高整体消费速度。详细可参考博客 https://www.jianshu.com/p/ad0e5424edbd 理解。
```

##### 关键代码

生产者：

```java
@Component
@Slf4j
public class Producer {
    @Autowired
    private KafkaTemplate<Object,Object> kafkaTemplate;

    //同步发送消息
    public SendResult syncSend(Integer id) throws ExecutionException, InterruptedException {
        // 创建 Demo01Message 消息
        TestMessage message = new TestMessage();
        message.setId(id);
        // 同步发送消息
        return kafkaTemplate.send(TestMessage.TOPIC, message).get();
    }

    //异步发送消息
    public ListenableFuture<SendResult<Object, Object>> asyncSend(Integer id) {
        // 创建 Demo01Message 消息
        TestMessage message = new TestMessage();
        message.setId(id);
        // 异步发送消息
        return kafkaTemplate.send(TestMessage.TOPIC, message);
    }
}
```

消费者：

```java
@Component
@Slf4j
public class Consumer {
    @KafkaListener(
            topics = TestMessage.TOPIC,
            groupId = "test-consumer-group"
    )
    public void  listen(ConsumerRecord<Integer,String> record){
        log.info("[onMessage][线程编号:{} 消息内容：{}]", Thread.currentThread().getId(), record);
    }
}
```

#### 保证kafka消息不丢失的方案

- 给 topic 设置 `replication.factor` 参数：这个值必须大于 1，要求每个 partition 必须有至少 2 个副本。
- 在 Kafka 服务端设置 `min.insync.replicas` 参数：这个值必须大于 1，这个是要求一个 leader 至少感知到有至少一个 follower 还跟自己保持联系，没掉队，这样才能确保 leader 挂了还有一个 follower 吧。
- 在 producer 端设置 `acks=all`：这个是要求每条数据，必须是**写入所有 replica 之后，才能认为是写成功了**。
- 在 producer 端设置 `retries=MAX`（很大很大很大的一个值，无限次重试的意思）：这个是**要求一旦写入失败，就无限重试**，卡在这里了

参考：https://gitee.com/jmlprivate/advanced-java/blob/master/docs/high-concurrency/how-to-ensure-the-reliable-transmission-of-messages.md