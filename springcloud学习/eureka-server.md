### eureka-server笔记

Eureka是基于REST（代表性状态转移）的服务，主要在AWS云中用于定位服务，以实现负载均衡和中间层服务器的故障转移。我们称此服务为Eureka服务器。Eureka还带有一个基于Java的客户端组件Eureka Client，它使与服务的交互变得更加容易。客户端还具有一个内置的负载平衡器，可以执行基本的循环负载平衡。在Netflix，更复杂的负载均衡器将Eureka包装起来，以基于流量，资源使用，错误条件等多种因素提供加权负载均衡，以提供出色的弹性。

**服务提供者**

服务注册

服务提供者在启动的时候会通过发送REST请求的方式将自己注册到Eureka Server上，同时自带了自身服务的元数据信息。Eureka Server在收到REST请求之后，将元数据信息存储到一个双层map中。

参数设置：eureka.client.register-with-eureka = true，若设置成false将不会启动注册操作。

服务同步：

如果服务注册中心之间互相注册，它会将请求转发给集群的其他注册中心，从而实现注册中心的同步。

服务续约：

注册完服务之后，服务提供者会维护一个心跳包，防止Eureka Server的“剔除任务”将该服务从服务列表排除。

**服务消费者**

当启动服务消费者的时候，它会发送REST请求给注册中心，获取上面的服务清单。为了性能考虑，Eureka Server会维护一份只读清单返回给客户端，同时该清单每30s更新一次。

服务调用

服务消费者在获取服务清单后，通过服务名可以获得具体提供服务的实例名和该实例的元数据信息。

服务下线

当服务实例运行正常的关闭操作时，它会触发一个服务下线的请求给Eureka Server告诉注册中心，我要下线，服务端在收到请求之后，将该服务设置成下线（DOWN），并把下线事件传播出去。

**服务注册中心**

失效剔除

服务实例并不一定会正常下线，可能由于内存溢出等原因使得服务不能正常工作，而服务注册中心并未收到服务下线的请求，为了将这些事例剔除。Eureka Server会创建一个定时任务将没有续约的服务剔除出去。

自我保护

Eureka在运行期间，会统计心跳失败的比例在15分钟是否低于85%，Eureka Server会将实例信息保护起来，让他们不过期。但是在保护期内若出现问题，那么客户端很容易拿到实际不存在的服务实例，就回出现调用失败的情况。比如请求重试、断路器等。

1.pom.xml配置

```xml
<dependency>
   <groupId>org.springframework.cloud</groupId>
   <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

2.启用eureka

```java
@EnableEurekaServer
@SpringBootApplication
public class DemoApplication {

   public static void main(String[] args) {
      SpringApplication.run(DemoApplication.class, args);
   }

}
```

3.配置文件

```yaml
server:
  port: 8001 #指定运行端口
spring:
  application:
    name: eureka-server #指定服务名称
eureka:
  instance:
    hostname: localhost #指定主机地址
  client:
    fetch-registry: false #指定是否要从注册中心获取服务（注册中心不需要开启）
    register-with-eureka: false #指定是否要注册到注册中心（注册中心不需要开启）
  server:
    enable-self-preservation: false #关闭保护模式
```

4.运行后访问http://localhost:8001/ 看到注册中心界面

![image-20200512110744109](C:\Users\19349\AppData\Roaming\Typora\typora-user-images\image-20200512110744109.png)





问题1：

```
om.sun.jersey.api.client.ClientHandlerException: java.net.ConnectException: 拒绝连接 (Connection refused)
```



```java
@ConfigurationProperties(EurekaClientConfigBean.PREFIX)
public class EurekaClientConfigBean implements EurekaClientConfig, Ordered {

/**
 * Default Eureka URL.
 */
public static final String DEFAULT_URL = "http://localhost:8761" + DEFAULT_PREFIX
      + "/";
    private Map<String, String> serviceUrl = new HashMap<>();

    {
        this.serviceUrl.put(DEFAULT_ZONE, DEFAULT_URL);
    }
}
```

增加配置：

```
client:
  service-url:
    defaultZone: http://localhost:8001/eureka/
```

