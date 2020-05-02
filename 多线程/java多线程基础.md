### 多线程基础

####  一、创建线程的基本接口

1.继承Thread类

```java
public class Demo {
    public static class MyThread extends Thread {
        @Override
        public void run() {
            System.out.println("MyThread");
        }
    }

    public static void main(String[] args) {
        Thread myThread = new MyThread();
        myThread.start();
    }
}
```

2.实现runable接口

```java
	public class MyThread implements Runnable{
        @Override
        public void run() {
            System.out.println("MyThread");
        }
    }
    public static void main(String[] args) {
        MyThread thread = new MyThread();
        Thread tmpTh = new Thread(thread);
        tmpTh.start();
	}
```

#####  二、线程常用接口

- currentThread()：静态方法，返回对当前正在执行的线程对象的引用；
- start()：开始执行线程的方法，java虚拟机会调用线程内的run()方法；
- yield()：yield在英语里有放弃的意思，同样，这里的yield()指的是当前线程愿意让出对当前处理器的占用。这里需要注意的是，就算当前线程调用了yield()方法，程序在调度的时候，也还有可能继续运行这个线程的；
- sleep()：静态方法，使当前线程睡眠一段时间；
- join()：使当前线程等待另一个线程执行完毕之后再继续执行，内部调用的是Object类的wait方法实现的；



#### 三、线程状态切换

![线程状态转换图](http://concurrent.redspider.group/article/01/imgs/%E7%BA%BF%E7%A8%8B%E7%8A%B6%E6%80%81%E8%BD%AC%E6%8D%A2%E5%9B%BE.png)

阻塞状态是指线程因为某种原因放弃了 cpu 使用权，也即让出了 cpu timeslice，暂时停止运行。
直到线程进入可运行(runnable)状态，才有机会再次获得 cpu timeslice 转到运行(running)状
态。阻塞的情况分三种：
等待阻塞（o.wait->等待对列） ：
运行(running)的线程执行 o.wait()方法， JVM 会把该线程放入等待队列(waitting queue)中。
同步阻塞(lock->锁池)
运行(running)的线程在获取对象的同步锁时，若该同步锁被别的线程占用，则 JVM 会把该线程放入锁池(lock pool)中。
其他阻塞(sleep/join)
运行(running)的线程执行 Thread.sleep(long ms)或 t.join()方法，或者发出了 I/O 请求时，JVM 会把该线程置为阻塞状态。当 sleep()状态超时、 join()等待线程终止或者超时、或者 I/O处理完毕时，线程重新转入可运行(runnable)状态。  