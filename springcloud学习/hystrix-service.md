### Hystrix笔记

#### 一、概念

> hystrix是netflix开源的一个容灾框架，解决当外部依赖故障时拖垮业务系统、甚至引起雪崩的问题。

为什么需要Hystrix?

在大中型分布式系统中，通常系统很多依赖(HTTP,hession,Netty,Dubbo等)，在高并发访问下,这些依赖的稳定性与否对系统的影响非常大,但是依赖有很多不可控问题:如网络连接缓慢，资源繁忙，暂时不可用，服务脱机等。

当依赖阻塞时,大多数服务器的线程池就出现阻塞(BLOCK),影响整个线上服务的稳定性，在复杂的分布式架构的应用程序有很多的依赖，都会不可避免地在某些时候失败。高并发的依赖失败时如果没有隔离措施，当前应用服务就有被拖垮的风险。

#### 二、实战

### [在pom.xml中添加相关依赖](http://www.macrozheng.com/#/cloud/hystrix?id=在pomxml中添加相关依赖)

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>Copy to clipboardErrorCopied
```

### [在application.yml进行配置](http://www.macrozheng.com/#/cloud/hystrix?id=在applicationyml进行配置)

> 主要是配置了端口、注册中心地址及user-service的调用路径。

```yaml
server:
  port: 8401
spring:
  application:
    name: hystrix-service
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8001/eureka/
service-url:
  user-service: http://user-serviceCopy to clipboardErrorCopied
```

### [在启动类上添加@EnableCircuitBreaker来开启Hystrix的断路器功能](http://www.macrozheng.com/#/cloud/hystrix?id=在启动类上添加enablecircuitbreaker来开启hystrix的断路器功能)

```java
@EnableCircuitBreaker
@EnableDiscoveryClient
@SpringBootApplication
public class HystrixServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HystrixServiceApplication.class, args);
    }Copy to clipboardErrorCopied
```

### [创建UserHystrixController接口用于调用user-service服务](http://www.macrozheng.com/#/cloud/hystrix?id=创建userhystrixcontroller接口用于调用user-service服务)

## [服务降级演示](http://www.macrozheng.com/#/cloud/hystrix?id=服务降级演示)

- 在UserHystrixController中添加用于测试服务降级的接口：

```java
@GetMapping("/testFallback/{id}")
public CommonResult testFallback(@PathVariable Long id) {
    return userService.getUser(id);
}Copy to clipboardErrorCopied
```

- 在UserService中添加调用方法与服务降级方法，方法上需要添加@HystrixCommand注解：

```java
@HystrixCommand(fallbackMethod = "getDefaultUser")
public CommonResult getUser(Long id) {
    return restTemplate.getForObject(userServiceUrl + "/user/{1}", CommonResult.class, id);
}

public CommonResult getDefaultUser(@PathVariable Long id) {
    User defaultUser = new User(-1L, "defaultUser", "123456");
    return new CommonResult<>(defaultUser);
}


```

![image-20200517205115649](C:\Users\19349\AppData\Roaming\Typora\typora-user-images\image-20200517205115649.png)

- 调用接口进行测试：http://localhost:8401/user/testFallback/1

![img](http://www.macrozheng.com/images/springcloud_hystrix_02.png)

- 关闭user-service服务重新测试该接口，发现已经发生了服务降级：

![img](http://www.macrozheng.com/images/springcloud_hystrix_03.png)

## [@HystrixCommand详解](http://www.macrozheng.com/#/cloud/hystrix?id=hystrixcommand详解)

### [@HystrixCommand中的常用参数](http://www.macrozheng.com/#/cloud/hystrix?id=hystrixcommand中的常用参数)

- fallbackMethod：指定服务降级处理方法；
- ignoreExceptions：忽略某些异常，不发生服务降级；
- commandKey：命令名称，用于区分不同的命令；
- groupKey：分组名称，Hystrix会根据不同的分组来统计命令的告警及仪表盘信息；
- threadPoolKey：线程池名称，用于划分线程池。