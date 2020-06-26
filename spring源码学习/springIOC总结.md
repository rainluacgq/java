###  `spring IOC`总结

### 一、IOC概念

Spring 通过一个配置文件描述 Bean 及 Bean 之间的依赖关系，利用 Java 语言的反射功能实例化
Bean 并建立 Bean 之间的依赖关系。 Spring 的 IoC 容器在完成这些底层工作的基础上，还提供
了 Bean 实例缓存、生命周期管理、 Bean 实例代理、事件发布、资源装载等高级服务。  

![img](https://mmbiz.qpic.cn/mmbiz_jpg/2BGWl1qPxib03XHfQ7GJx5IicSL37jAgtI4yAOibB9y1bjCG9xsqyHbiabdxTs8NOEq1fgicsMefAhkFM5EcETBQDBg/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

1. 根据Bean配置信息在容器内部创建Bean定义注册表
2. 根据注册表加载、实例化bean、建立Bean与Bean之间的依赖关系
3. 将这些准备就绪的Bean放到Map缓存池中，等待应用程序调用

### 二、实现

BeanFactory-框架基础设施

BeanFactory 是 Spring 框架的基础设施，面向 Spring 本身；  

![img](https://mmbiz.qpic.cn/mmbiz_png/2BGWl1qPxib03XHfQ7GJx5IicSL37jAgtIAwmWgOyrHRFqJqr6TGHgib3AW6EqyTZeVsFkeXTGxVjFVPVozKIPbSg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

BeanFactory提供了

![image-20200510160317595](https://github.com/rainluacgq/java/blob/master/spring%E6%BA%90%E7%A0%81%E5%AD%A6%E4%B9%A0/pic/image-20200510160317595.png)

![img](https://blog-1259634016.cos.ap-chengdu.myqcloud.com/img/20190714074041.png)



ApplicationContext 面向使用Spring 框架的开发者，几乎所有的应用场合我们都直接使用 ApplicationContext 而非底层
的 BeanFactory。  

![img](https://mmbiz.qpic.cn/mmbiz_png/2BGWl1qPxib03XHfQ7GJx5IicSL37jAgtIFXB9xdiaFVfSoFFnzJV0t0XE7KqtwWeaic0Z1clEWGNRUD1QShCyhSGw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

BeanFactory中bean的生命周期

![img](https://mmbiz.qpic.cn/mmbiz_png/2BGWl1qPxib03XHfQ7GJx5IicSL37jAgtIF3zqLRDdibeEhYNkMbAE6JJaLQXtuQWiabJb50Fibcic1BByYWpnvb0ecw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)





ApplicationContext中bean的生命周期

![img](https://mmbiz.qpic.cn/mmbiz_png/2BGWl1qPxib03XHfQ7GJx5IicSL37jAgtI4XV6mibSicdyqjo1AmV3OqWemmOGv1vT5qicLUO9icS3ZRl45I54OllF4g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

### Spring 中的 bean 生命周期?

这部分网上有很多文章都讲到了，下面的内容整理自：https://yemengying.com/2016/07/14/spring-bean-life-cycle/ ，除了这篇文章，再推荐一篇很不错的文章 ：https://www.cnblogs.com/zrtqsk/p/3735273.html 。

- Bean 容器找到配置文件中 Spring Bean 的定义。
- Bean 容器利用 Java Reflection API 创建一个Bean的实例。
- 如果涉及到一些属性值 利用 `set()`方法设置一些属性值。
- 如果 Bean 实现了 `BeanNameAware` 接口，调用 `setBeanName()`方法，传入Bean的名字。
- 如果 Bean 实现了 `BeanClassLoaderAware` 接口，调用 `setBeanClassLoader()`方法，传入 `ClassLoader`对象的实例。
- 与上面的类似，如果实现了其他 `*.Aware`接口，就调用相应的方法。
- 如果有和加载这个 Bean 的 Spring 容器相关的 `BeanPostProcessor` 对象，执行`postProcessBeforeInitialization()` 方法
- 如果Bean实现了`InitializingBean`接口，执行`afterPropertiesSet()`方法。
- 如果 Bean 在配置文件中的定义包含 init-method 属性，执行指定的方法。
- 如果有和加载这个 Bean的 Spring 容器相关的 `BeanPostProcessor` 对象，执行`postProcessAfterInitialization()` 方法
- 当要销毁 Bean 的时候，如果 Bean 实现了 `DisposableBean` 接口，执行 `destroy()` 方法。
- 当要销毁 Bean 的时候，如果 Bean 在配置文件中的定义包含 destroy-method 属性，执行指定的方法。

图示：

[![Spring Bean 生命周期](https://camo.githubusercontent.com/d275f148f8928ccc284180731f90991891be1a35/687474703a2f2f6d792d626c6f672d746f2d7573652e6f73732d636e2d6265696a696e672e616c6979756e63732e636f6d2f31382d392d31372f34383337363237322e6a7067)](https://camo.githubusercontent.com/d275f148f8928ccc284180731f90991891be1a35/687474703a2f2f6d792d626c6f672d746f2d7573652e6f73732d636e2d6265696a696e672e616c6979756e63732e636f6d2f31382d392d31372f34383337363237322e6a7067)

与之比较类似的中文版本:

[![Spring Bean 生命周期](https://camo.githubusercontent.com/a3d4415162d30d4659779f6db3717f9a68fd3c97/687474703a2f2f6d792d626c6f672d746f2d7573652e6f73732d636e2d6265696a696e672e616c6979756e63732e636f6d2f31382d392d31372f353439363430372e6a7067)](https://camo.githubusercontent.com/a3d4415162d30d4659779f6db3717f9a68fd3c97/687474703a2f2f6d792d626c6f672d746f2d7573652e6f73732d636e2d6265696a696e672e616c6979756e63732e636f6d2f31382d392d31372f353439363430372e6a7067)







三、源码

```java
public void refresh() throws BeansException, IllegalStateException {
   synchronized (this.startupShutdownMonitor) {
      // Prepare this context for refreshing.
      prepareRefresh();

      // Tell the subclass to refresh the internal bean factory.
      ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

      // Prepare the bean factory for use in this context.
      prepareBeanFactory(beanFactory);

      try {
         // Allows post-processing of the bean factory in context subclasses.
         postProcessBeanFactory(beanFactory);

         // Invoke factory processors registered as beans in the context.
         invokeBeanFactoryPostProcessors(beanFactory);

         // Register bean processors that intercept bean creation.
         registerBeanPostProcessors(beanFactory);

         // Initialize message source for this context. //和国际化相关
         initMessageSource();

         // Initialize event multicaster for this context.
         initApplicationEventMulticaster();

         // Initialize other special beans in specific context subclasses.
         onRefresh();

         // Check for listener beans and register them.
         registerListeners();

         // Instantiate all remaining (non-lazy-init) singletons.
         finishBeanFactoryInitialization(beanFactory);

         // Last step: publish corresponding event.
         finishRefresh();
      }

      catch (BeansException ex) {
         if (logger.isWarnEnabled()) {
            logger.warn("Exception encountered during context initialization - " +
                  "cancelling refresh attempt: " + ex);
         }

         // Destroy already created singletons to avoid dangling resources.
         destroyBeans();

         // Reset 'active' flag.
         cancelRefresh(ex);

         // Propagate exception to caller.
         throw ex;
      }

      finally {
         // Reset common introspection caches in Spring's core, since we
         // might not ever need metadata for singleton beans anymore...
         resetCommonCaches();
      }
   }
}
```

看一下beanDefinetion

![image-20200510165854980](https://github.com/rainluacgq/java/blob/master/spring%E6%BA%90%E7%A0%81%E5%AD%A6%E4%B9%A0/pic/image-20200510165854980.png)

参考：

https://blog.csdn.net/nuomizhende45/article/details/81158383

https://www.jianshu.com/p/17b66e6390fd

https://blog.csdn.net/nuomizhende45/article/details/81158383





Q1：循环依赖问题

在Spring的DefaultSingletonBeanRegistry类中，你会赫然发现类上方挂着这三个Map：

- singletonObjects 它是我们最熟悉的朋友，俗称“单例池”“容器”，缓存创建完成单例Bean的地方。
- singletonFactories 映射创建Bean的原始工厂
- earlySingletonObjects 映射Bean的早期引用，也就是说在这个Map里的Bean不是完整的，甚至还不能称之为“Bean”，只是一个Instance.

```java
/** Cache of singleton objects: bean name to bean instance. */
private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

/** Cache of singleton factories: bean name to ObjectFactory. */
private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);

/** Cache of early singleton objects: bean name to bean instance. */
private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);
```

参考：https://mp.weixin.qq.com/s/2u7k_jt-3p_dgTXy4tB7Tg