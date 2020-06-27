### Hystrix笔记

#### 一、概念

> hystrix是netflix开源的一个容灾框架，解决当外部依赖故障时拖垮业务系统、甚至引起雪崩的问题。

为什么需要Hystrix?

在大中型分布式系统中，通常系统很多依赖(HTTP,hession,Netty,Dubbo等)，在高并发访问下,这些依赖的稳定性与否对系统的影响非常大,但是依赖有很多不可控问题:如网络连接缓慢，资源繁忙，暂时不可用，服务脱机等。

当依赖阻塞时,大多数服务器的线程池就出现阻塞(BLOCK),影响整个线上服务的稳定性，在复杂的分布式架构的应用程序有很多的依赖，都会不可避免地在某些时候失败。高并发的依赖失败时如果没有隔离措施，当前应用服务就有被拖垮的风险。

**Hystrix主要提供四个功能：断路器、隔离机制、请求聚合和请求缓存**

断路器（Circuit Breaker）：用于提供熔断降级的功能。控制是否可以发起对依赖服务的请求结果，然后做统计和计算。

- Hystrix利用线程池或者信号量机制提供依赖服务的隔离，每个依赖服务都使用独立的线程池，这样的好处是一个依赖服务发生故障时，对当前的服务影响会限制在这个线程池内部，不会影响对其他依赖服务的请求，代价是当前服务中创建了很多的线程，需要不小的线程上下文切换开销，特别是对低时延的调用有较大的影响。

- Hystrix使用信号量机制去控制针对某个依赖服务的并发访问情况，这样的请求隔离非常的轻量级，不需要显式创建线程池，但是信号量机制无法处理访问依赖服务的请求时间变长的情况。

- 请求聚合：使用HystrixCollapser对前端的多个请求聚合成一个请求发送到后端；

- 请求缓存：HystrixCommand和HystrixObservableCommand实现了对请求的缓存，假如某个上下文多个请求同时到达相同参数的查询，利用请求缓存的功能，可以减少对后端系统的压力。

**Hystrix的工作流程如下所示**：

![img](https://upload-images.jianshu.io/upload_images/14126519-51a01b643c944562.png?imageMogr2/auto-orient/strip|imageView2/2/format/webp)

（1）构建HystrixCommand或者`HystrixObservableCommand` ， 在使用Hystrix的过程中，会对**依赖服务**的调用请求封装成**命令对象**，Hystrix 对 **命令对象**抽象了两个抽象类：`HystrixCommand` 和`HystrixObservableCommand` 。`HystrixCommand` 表示的**命令对象**会返回一个唯一返回值：`HystrixObservableCommand` 表示的**命令对象** 会返回多个返回值

（2）执行命令

Hystrix中共有4种方式执行命令，如下所示：

| 执行方式       | 说明                                                         | 可用对象                   |
| :------------- | :----------------------------------------------------------- | :------------------------- |
| `execute()`    | 阻塞式同步执行，返回依赖服务的单一返回结果(或者抛出异常)     | `HystrixCommand`           |
| `queue()`      | 基于Future的异步方式执行，返回依赖服务的单一返回结果(或者抛出异常) | `HystrixCommand`           |
| `observe()`    | 基于Rxjava的Observable方式，返回通过Observable表示的依赖服务返回结果,代调用代码先执行(Hot Obserable) | `HystrixObservableCommand` |
| `toObvsevable` | 基于Rxjava的Observable方式，返回通过Observable表示的依赖服务返回结果,执行代码等到真正订阅的时候才会执行(cold observable) | `HystrixObservableCommand` |

（3）检查是否有缓存

如果当前命令对象配置了允许从`结果缓存`中取返回结果，并且在`结果缓存`中已经缓存了请求结果，则缓存的请求结果会立刻通过Observable的格式返回。否则执行下一步骤。

（4）检查断路器状态

判断一下当前断路器的断路状态是否打开。如果断路器状态为`打开`状态，则Hystrix将不会执行此Command命令，直接执行**步骤8** 调用Fallback； 如果断路器状态是`关闭`，则执行 **步骤5** 检查是否有足够的资源运行 Command命令

（5）检查资源(线程池/队列/信号量)是否已满？

如果当前要执行的Command命令 先关连的线程池 和队列(或者信号量)资源已经满了，Hystrix将不会运行 Command命令，直接执行 **步骤8**的Fallback降级处理；如果未满，表示有剩余的资源执行Command命令，则执行**步骤6**

（6）执行 `HystrixObservableCommand.construct()` 或者 `HystrixCommand.run()`

当经过**步骤5** 判断，有足够的资源执行Command命令时，本步骤将调用Command命令运行方法，基于不同类型的Command，有如下两种两种运行方式：

| 运行方式                               | 说明                                                         |
| :------------------------------------- | :----------------------------------------------------------- |
| `HystrixCommand.run()`                 | 返回一个处理结果或者抛出一个异常                             |
| `HystrixObservableCommand.construct()` | 返回一个Observable表示的结果(可能多个)，或者 基于`onError`的错误通知 |

如果`run()` 或者`construct()`方法 的`真实执行时间`超过了Command设置的`超时时间阈值`, 则**当前则执行线程**（或者是独立的定时器线程）将会抛出`TimeoutException`。抛出超时异常TimeoutException，后，将执行**步骤8**的Fallback降级处理。即使`run()`或者`construct()`执行没有被取消或中断，最终能够处理返回结果，但在降级处理逻辑中，将会抛弃`run()`或`construct()`方法的返回结果，而返回Fallback降级处理结果。

（7）计算断路器的健康状况

Hystrix 会统计Command命令执行执行过程中的**成功数**、**失败数**、**拒绝数**和**超时数**,将这些信息记录到**断路器(Circuit Breaker)**中。断路器将上述统计按照**时间窗**的形式记录到一个定长数组中。断路器根据时间窗内的统计数据去判定请求什么时候可以被熔断，熔断后，在接下来一段恢复周期内，相同的请求过来后会直接被熔断。当再次校验，如果健康监测通过后，熔断开关将会被关闭。

（8）获取Fallback

当以下场景出现后，Hystrix将会尝试触发Fallback:

> - 步骤6 Command执行时抛出了任何异常；
> - 步骤4 断路器已经被打开
> - 步骤5 执行命令的线程池、队列或者信号量资源已满
> - 命令执行的时间超过阈值

（9）返回成功结果

如果 Hystrix 命令对象执行成功，将会返回结果，或者以`Observable`形式包装的结果

参考：https://www.jianshu.com/p/684b04b6c454

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