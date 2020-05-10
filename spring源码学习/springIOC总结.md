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

![image-20200510160317595](C:\Users\19349\AppData\Roaming\Typora\typora-user-images\image-20200510160317595.png)

![img](https://blog-1259634016.cos.ap-chengdu.myqcloud.com/img/20190714074041.png)



ApplicationContext 面向使用Spring 框架的开发者，几乎所有的应用场合我们都直接使用 ApplicationContext 而非底层
的 BeanFactory。  

![img](https://mmbiz.qpic.cn/mmbiz_png/2BGWl1qPxib03XHfQ7GJx5IicSL37jAgtIFXB9xdiaFVfSoFFnzJV0t0XE7KqtwWeaic0Z1clEWGNRUD1QShCyhSGw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

BeanFactory中bean的生命周期

![img](https://mmbiz.qpic.cn/mmbiz_png/2BGWl1qPxib03XHfQ7GJx5IicSL37jAgtIF3zqLRDdibeEhYNkMbAE6JJaLQXtuQWiabJb50Fibcic1BByYWpnvb0ecw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)





ApplicationContext中bean的生命周期

![img](https://mmbiz.qpic.cn/mmbiz_png/2BGWl1qPxib03XHfQ7GJx5IicSL37jAgtI4XV6mibSicdyqjo1AmV3OqWemmOGv1vT5qicLUO9icS3ZRl45I54OllF4g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

applicationContext的生命周期:

![img](https://mmbiz.qpic.cn/mmbiz_png/2BGWl1qPxib03XHfQ7GJx5IicSL37jAgtI4XV6mibSicdyqjo1AmV3OqWemmOGv1vT5qicLUO9icS3ZRl45I54OllF4g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)







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

参考：https://blog.csdn.net/nuomizhende45/article/details/81158383