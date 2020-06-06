### springboot线程池使用

一、关键参数

```java
package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/6/6
 */

@EnableAsync
@Configuration
public class ThreadPoolConfig {

    @Bean("taskExcuter")
    public ThreadPoolTaskExecutor asyncThreadPoolTaskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(200);
        executor.setQueueCapacity(25);
        executor.setKeepAliveSeconds(200);
        executor.setThreadNamePrefix("asyncThread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}
```

二、在需要使用线程池的方法使用@Async注解

```java
@Component
public class ThreadPoolService {
        private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolService.class);
        
        @Async
        public  void test() throws InterruptedException {
            LOGGER.info("current thread name:{} ,id{}",Thread.currentThread().getName(),
                    Thread.currentThread().getId());
            Thread.sleep(300);
        }

}
```

测试：

![image-20200606195105979](C:\Users\19349\AppData\Roaming\Typora\typora-user-images\image-20200606195105979.png)

**注意事项**
如下方式会使@Async失效

一、异步方法使用static修饰
二、异步类没有使用@Component注解（或其他注解）导致spring无法扫描到异步类
三、异步方法不能与被调用的异步方法在同一个类中
四、类中需要使用@Autowired或@Resource等注解自动注入，不能自己手动new对象
五、如果使用SpringBoot框架必须在启动类中增加@EnableAsync注解

源码地址：G:\java\git\java\spring源码学习

看一下SpringBoot中的线程池

```java
public class ThreadPoolTaskExecutor extends ExecutorConfigurationSupport
      implements AsyncListenableTaskExecutor, SchedulingTaskExecutor {

   private final Object poolSizeMonitor = new Object();

   private int corePoolSize = 1;

   private int maxPoolSize = Integer.MAX_VALUE;

   private int keepAliveSeconds = 60;

   private int queueCapacity = Integer.MAX_VALUE;

   private boolean allowCoreThreadTimeOut = false;
```

线程池初始化

```java
protected ExecutorService initializeExecutor(
      ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {

   BlockingQueue<Runnable> queue = createQueue(this.queueCapacity);

   ThreadPoolExecutor executor;
   if (this.taskDecorator != null) {
      executor = new ThreadPoolExecutor(
            this.corePoolSize, this.maxPoolSize, this.keepAliveSeconds, TimeUnit.SECONDS,
            queue, threadFactory, rejectedExecutionHandler) {
         @Override
         public void execute(Runnable command) {
            Runnable decorated = taskDecorator.decorate(command);
            if (decorated != command) {
               decoratedTaskMap.put(decorated, command);
            }
            super.execute(decorated);
         }
      };
   }
   else {
      executor = new ThreadPoolExecutor(
            this.corePoolSize, this.maxPoolSize, this.keepAliveSeconds, TimeUnit.SECONDS,
            queue, threadFactory, rejectedExecutionHandler);

   }

   if (this.allowCoreThreadTimeOut) {
      executor.allowCoreThreadTimeOut(true);
   }

   this.threadPoolExecutor = executor;
   return executor;
}
```

创建任务队列：

```java
protected BlockingQueue<Runnable> createQueue(int queueCapacity) {
   if (queueCapacity > 0) {
      return new LinkedBlockingQueue<>(queueCapacity);
   }
   else {
      return new SynchronousQueue<>();
   }
}
```