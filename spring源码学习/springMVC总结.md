###  `Spring MVC`总结

### 一、MVC概念

Spring 的模型-视图-控制器（MVC）框架是围绕一个 DispatcherServlet 来设计的，这个 Servlet会把请求分发给各个处理器，并支持可配置的处理器映射、视图渲染、本地化、时区与主题渲染等，甚至还能支持文件上传 。 

**对比之下，SpringBoot具有如下特征**：

1. 创建独立的 Spring 应用程序
2. 嵌入的 Tomcat，无需部署 WAR 文件
3. 简化 Maven 配置
4. 自动配置 Spring
5. 提供生产就绪型功能，如指标，健康检查和外部配置
6. 绝对没有代码生成和对 XML 没有要求配置 [1]  

#### 二、过程

先来看一下SpringMVC的初始化过程：

 ![image-20200626215342097](https://github.com/rainluacgq/java/blob/master/spring源码学习/pic/image-20200626215342097.png)

**SpringMVC的流程是什么？**

1. 用户发送请求至前端控制器DispatcherServlet；

2. DispatcherServlet收到请求后，调用HandlerMapping处理器映射器，请求获取Handle；

3. 处理器映射器根据请求url找到具体的处理器，生成处理器对象及处理器拦截器(如果有)一并返回给DispatcherServlet；

4. DispatcherServlet 调用 HandlerAdapter处理器适配器；

5. HandlerAdapter 经过适配调用 具体处理器(Handler，也叫后端控制器)；

6. Handler执行完成返回ModelAndView；

7. HandlerAdapter将Handler执行结果ModelAndView返回给DispatcherServlet；

8. DispatcherServlet将ModelAndView传给ViewResolver视图解析器进行解析；

9. ViewResolver解析后返回具体View；

10. DispatcherServlet对View进行渲染视图（即将模型数据填充至视图中）

11. DispatcherServlet响应用户。

![image-20200626220133375](https://github.com/rainluacgq/java/blob/master/spring源码学习/pic/image-20200626220133375.png)

参考：https://mp.weixin.qq.com/s/rFKcHpyzaITMzYhM3ob1sA

