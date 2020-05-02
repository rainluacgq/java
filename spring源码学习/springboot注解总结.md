#### springboot 总结

#### @SpringBootApplication注解学习

@SpringBootApplication 注解背后：

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = {
      @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
      @Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
```

重要的3个注解

1. `@SpringBootConfiguration`其实就是`@Configuration`

   `@Configuration` 和`@Bean`配合使用，使用这两个注解可以创建简单的spring配置类

   ```java
   @Configuration
   public class MasterMyBatisConfig {
       @Bean(name = "master")
       public DataSource dataSource() {
           return DataSourceBuilder.create().build();
       }
   ```

2.`@ComponentScan`

功能：自动扫描并加载符合条件的组件（如`@Component`,`@Respository`,或者bean定义），最终将bean加载到IOC容器中

3.`@EnableAutoConfiguration`

源代码定义：

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import(AutoConfigurationImportSelector.class)
```

`@EnableAutoConfiguration`借助`@import`的帮助将符合自动配置的bean加载到IOC容器

比如 `springboot-start-web` 会自动添加`tomcat` `springMVC`

![image-20200430215129666](https://github.com/rainluacgq/java/blob/master/spring%E6%BA%90%E7%A0%81%E5%AD%A6%E4%B9%A0/pic/image-20200430215129666.png)

最关键的注解 `@Import(AutoConfigurationImportSelector.class)` 背后的英雄是 `springFactoryLoader`

![image-20200430220040503](https://github.com/rainluacgq/java/blob/master/spring%E6%BA%90%E7%A0%81%E5%AD%A6%E4%B9%A0/pic/image-20200430220040503.png

从classpath中搜寻所有的`MATA-INF/spring.factories`配置文件，并将其中的`org.springframework.boot.autoconfigure.EnableAutoConfiguration` 对应的配置通过反射实例化，并加载到IOC容器中

```java
一、getCandidateConfigurations会将配置信息已List返回
```

![image-20200501100051891](C:\Users\19349\AppData\Roaming\Typora\typora-user-images\image-20200501100051891.png)
