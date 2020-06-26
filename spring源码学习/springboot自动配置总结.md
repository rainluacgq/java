

#### springboot 自动配置总结

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

我们可以通过basePackages等属性来细粒度的定制@ComponentScan注解自动扫描类的范围

3.`@EnableAutoConfiguration`

`@EnableAutoConfiguration`是springBoot实现自动配置的关键。

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

![image-20200430220040503](https://github.com/rainluacgq/java/blob/master/spring%E6%BA%90%E7%A0%81%E5%AD%A6%E4%B9%A0/pic/image-20200430220040503.png)

从classpath中搜寻所有的`MATA-INF/spring.factories`配置文件，并将其中的`org.springframework.boot.autoconfigure.EnableAutoConfiguration` 对应的配置通过反射实例化，并加载到IOC容器中

```java
protected List<String> getCandidateConfigurations(AnnotationMetadata metadata,
      AnnotationAttributes attributes) {
   List<String> configurations = SpringFactoriesLoader.loadFactoryNames(
         getSpringFactoriesLoaderFactoryClass(), getBeanClassLoader());
   Assert.notEmpty(configurations,
         "No auto configuration classes found in META-INF/spring.factories. If you "
               + "are using a custom packaging, make sure that file is correct.");
   return configurations;
}

public final class SpringFactoriesLoader {

	/**
	 * The location to look for factories.
	 * <p>Can be present in multiple JAR files.
	 */
	public static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories";
}
```

1.loadFactoryNames（）方法加载SpringBoot默认的需要配置的类

2.SpringFactoriesLoader的成员变量默认约定了加载加载自动配置的路径

```java
一、getCandidateConfigurations会将配置信息已List返回
```

![image-20200501100051891](https://github.com/rainluacgq/java/blob/master/spring%E6%BA%90%E7%A0%81%E5%AD%A6%E4%B9%A0/pic/image-20200501100051891.png)
