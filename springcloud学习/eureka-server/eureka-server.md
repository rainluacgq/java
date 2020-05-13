### eureka-server笔记

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