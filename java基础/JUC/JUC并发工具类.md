### JAVA的并发工具类总结

#### 1.等待多线程完成的CountDownLatch

```java
package thread;

import java.util.concurrent.CountDownLatch;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/5/30
 */
public class CountDownLatchTest {

    static CountDownLatch c  = new CountDownLatch(2);

    public static void main(String[] args) throws InterruptedException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(1);
                c.countDown();
                System.out.println(2);
                c.countDown();
            }
        }).start();
        c.await();  //阻塞当前线程
        System.out.println(3);

    }
}
```

调用CountDownLatch的countDown时，会将初始化的计数器减1。这里减1，可以在N个线程中使用，也可在一个线程中使用。

如果想让主线程等待指定时间，可使用await(long timeout, TimeUnit unit)等待，不会阻塞当前线程

```java
public boolean await(long timeout, TimeUnit unit)
    throws InterruptedException {
    return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
}
```



### 同步屏障CyclicBarrier

可循环使用（Cyclic）的屏障（Barrier），让一组线程到达一个屏障时被阻塞，直到最后一个线程到达屏障时，屏障才会开门，所有被拦截的线程才能继续运行。

```java
package thread;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/5/30
 */
public class CyclicBarrierTest {
    static CyclicBarrier c = new CyclicBarrier(2);

    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    c.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println(1);
            }
        }).start();

        try {
            c.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(2);
    }
}
```

输出

1  2或者2 1

CyclicBarrier还提供了更高级的构造函数CyclicBarrier(int parties, Runnable barrierAction),用于线程到达屏障时，优先执行barrierAction

```java
public CyclicBarrier(int parties, Runnable barrierAction) {
    if (parties <= 0) throw new IllegalArgumentException();
    this.parties = parties;
    this.count = parties;
    this.barrierCommand = barrierAction;
}
```



### 控制并发数量的Semaphore

Semaphore（信号量）用来控制同时访问特定资源的线程数量，通过协调各个线程，保证合理的使用公共资源。

1.应用场景

Semaphore可以用于流量控制，特别是公共资源有限的应用场景。比如数据库连接。

比如要读取几万文件的数据，都是IO密集型的任务，可以启动几十个线程去 并发读取。但是若读取结果还需要存储到数据库，但是数据库连接只有10个，这时就必须控制只有10个线程同时获取数据库连接保存数据。

```java
package thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/5/30
 */
public class SemaphoreTest {
    private  static final int THREAD_COUNT = 30;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);

    private static Semaphore s = new Semaphore(10);

    public static void main(String[] args) {
        for (int i=0;i<THREAD_COUNT;i++){
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        s.acquire();    //获取许可证 也可使用s.tryAcquire();
                        System.out.println("save data");
                        s.release();    //归还许可证
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        threadPool.shutdown();
    }
}
```

Semaphore的构造方法接受整形数字，表示许可证数量。

使用方法如上所示，先获取许可证，使用完之后归还许可证。



#### 线程间交换数据的Exchanger

Exchanger（交换者）用于线程间协作的工具类。用于线程间的数据交换，他提供一个同步点，在同步点，两线程可以交换彼此的数据，线程通过exange方法交换数据，如果第一个线程先执行exchange方法，他会一直等待第二个线程也执行exchange方法。当两个线程都到达同步点时，就可以交换数据。

```java
package thread;

import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/5/30
 */
public class ExchangerTest {
    private static final Exchanger<String> exgr = new Exchanger<String>();
    private static ExecutorService threadPool = Executors.newFixedThreadPool(2);

    public static void main(String[] args) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                String A = "银行流水A";     //A 录入的银行流水
                try {
                    exgr.exchange(A);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                String B = "银行流水B"; //B录入的银行流水
                try {
                    String A = exgr.exchange(B);
                    System.out.println("A 和B 是否一致" + A.equals(B)+
                    "A录入的是：" + A  + ",B录入的是：" +B);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        threadPool.shutdown();
    }
}
```