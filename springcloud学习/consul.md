### Consul笔记

目前主流的注册中心

| Feature              | eureka                       | Consul                 | zookeeper             | etcd              |
| :------------------- | :--------------------------- | :--------------------- | :-------------------- | :---------------- |
| 服务健康检查         | 可配支持                     | 服务状态，内存，硬盘等 | (弱)长连接，keepalive | 连接心跳          |
| 多数据中心           | —                            | 支持                   | —                     | —                 |
| kv 存储服务          | —                            | 支持                   | 支持                  | 支持              |
| 一致性算法           | —                            | raft                   | paxos增强             | raft              |
| cap                  | ap                           | cp                     | cp                    | cp                |
| 使用接口(多语言能力) | http（sidecar）              | 支持 http 和 dns       | 客户端                | http/grpc         |
| watch 支持           | 支持 long polling/大部分增量 | 全量/支持long polling  | 支持                  | 支持 long polling |
| 自身监控             | metrics                      | metrics                | —                     | metrics           |
| 安全                 | —                            | acl /https             | acl                   | https 支持（弱）  |
| spring cloud 集成    | 已支持                       | 已支持                 | 已支持                | 已支持            |

------

CAP 原则又称 CAP 定理，指的是在一个分布式系统中， Consistency（一致性）、 Availability
（可用性）、 Partition tolerance（分区容错性），三者不可得兼。
		一致性（C）：

1. 在分布式系统中的所有数据备份，在同一时刻是否同样的值。（等同于所有节点访问同一份
   最新的数据副本）
   可用性（ A）：
2. 在集群中一部分节点故障后，集群整体是否还能响应客户端的读写请求。（对数据更新具备
   高可用性）
   分区容忍性（P） ：
3. 以实际效果而言，分区相当于对通信的时限要求。系统如果不能在时限内达成数据一致性，
   就意味着发生了分区的情况，必须就当前操作在 C 和 A 之间做出选择。  

#### Consul概念

Consul是HashiCorp公司推出的开源软件，提供了微服务系统中的服务治理、配置中心、控制总线等功能。这些功能中的每一个都可以根据需要单独使用，也可以一起使用以构建全方位的服务网格，总之Consul提供了一种完整的服务网格解决方案。

Spring Cloud Consul 具有如下特性：

- 支持服务治理：Consul作为注册中心时，微服务中的应用可以向Consul注册自己，并且可以从Consul获取其他应用信息；
- 支持客户端负载均衡：包括Ribbon和Spring Cloud LoadBalancer；
- 支持Zuul：当Zuul作为网关时，可以从Consul中注册和发现应用；
- 支持分布式配置管理：Consul作为配置中心时，使用键值对来存储配置信息；
- 支持控制总线：可以在整个微服务系统中通过 Control Bus 分发事件消息。



### 实战

##### 一、下载consul

地址：https://www.consul.io/downloads.html

- 下载完成后只有一个exe文件，双击运行；
- 在命令行中输入以下命令可以查看版本号：

```shell
consul --versionCopy to clipboardErrorCopied
```

- 查看版本号信息如下：

```bash
Consul v1.6.1
Protocol 2 spoken by default, understands 2 to 3 (agent will automatically use protocol >2 when speaking to compatible agents)Copy to clipboardErrorCopied
```

- 使用开发模式启动：

```shell
consul agent -dev 
```

#### 二、集成到springcloud

修改依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-consul-discovery</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

配置

```yaml
server:
  port: 8206
spring:
  application:
    name: consul-user-service
  cloud:
    consul: #Consul服务注册发现配置
      host: localhost
      port: 8500
      discovery:
        service-name: ${spring.application.name}
```

启用client

```java
@EnableDiscoveryClient
@SpringBootApplication
public class ConsulUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsulUserServiceApplication.class, args);
    }

}
```

若成功注册到consul到注册中心，可看到如下信息：

![image-20200516191402878](C:\Users\19349\AppData\Roaming\Typora\typora-user-images\image-20200516191402878.png)      