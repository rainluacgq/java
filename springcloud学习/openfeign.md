### 集成Ribbon和Hystrix的OpenFeign

### 概念一览

Feign是声明式的服务调用工具，我们只需创建一个接口并用注解的方式来配置它，就可以实现对某个服务接口的调用，简化了直接使用RestTemplate来调用服务接口的开发量。Feign具备可插拔的注解支持，同时支持Feign注解、JAX-RS注解及SpringMvc注解。当使用Feign时，Spring Cloud集成了Ribbon和Eureka以提供负载均衡的服务调用及基于Hystrix的服务容错保护功能。

### 实战

加入依赖

```xml
<dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
```
集成hystrix进行服务降级

```java
feign:
  hystrix:
    enabled: true
```

修改@FeignClient注解中的参数，设置fallback为FallbackService.class即可。

```java
@FeignClient(value = "test",fallback = FeignFallBackService.class)
public interface FeignTestService {
	/*code */
}
```

实现降级代码

```java
@Service
public class FeignFallBackService implements FeignTestService{
	/*code*/
}
```