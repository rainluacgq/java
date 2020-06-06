### Ribbon笔记

1.pom.xml配置

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
</dependency>
```

2.启用eureka

```java
@EnableEurekaClient
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
  port: 8101 #运行端口号
spring:
  application:
    name: eureka-client #服务名称
eureka:
  client:
    register-with-eureka: true #注册到Eureka的注册中心
    fetch-registry: true #获取注册实例列表
    service-url:
      defaultZone: http://localhost:8001/eureka/ #配置注册中心地址

```

4.运行后访问http://localhost:8001/ 看到注册中心界面

![image-20200512113739056](C:\Users\19349\AppData\Roaming\Typora\typora-user-images\image-20200512113739056.png)



### 二、错误

问题1：

![image-20200512111404766](C:\Users\19349\AppData\Roaming\Typora\typora-user-images\image-20200512111404766.png)

未添加start-web

问题2：第一次加载ribbon提示time out

```java
java.net.SocketTimeoutException: Read timed out
```

解决方法：

```yml
ribbon:
  eager-load:
    enabled: true
    clients: client1, client2, client3
```

参考：https://cloud.spring.io/spring-cloud-netflix/multi/multi_spring-cloud-ribbon.html#ribbon-child-context-eager-load