### ReentrantLock总结

ReentrantLock意思为可重入锁，指的是一个线程能够对一个临界资源重复加锁。

![image-20200503182558940](C:\Users\19349\AppData\Roaming\Typora\typora-user-images\image-20200503182558940.png)

代码示例：

```java
// **************************Synchronized的使用方式**************************
// 1.用于代码块
synchronized (this) {}
// 2.用于对象
synchronized (object) {}
// 3.用于方法
public synchronized void test () {}
// 4.可重入
for (int i = 0; i < 100; i++) {
  synchronized (this) {}
}
// **************************ReentrantLock的使用方式**************************
public void test () throw Exception {
  // 1.初始化选择公平锁、非公平锁
  ReentrantLock lock = new ReentrantLock(true);
  // 2.可用于代码块
  lock.lock();
  try {
    try {
      // 3.支持多种加锁方式，比较灵活; 具有可重入特性
      if(lock.tryLock(100, TimeUnit.MILLISECONDS)){ }
    } finally {
      // 4.手动释放锁
      lock.unlock()
    }
  } finally {
    lock.unlock();
  }
}
```

二、原理了解

非公平锁的加锁过程：

```java
// java.util.concurrent.locks.ReentrantLock#NonfairSync

// 非公平锁
static final class NonfairSync extends Sync {
  ...
  final void lock() {
    if (compareAndSetState(0, 1))
      setExclusiveOwnerThread(Thread.currentThread());
    else
      acquire(1);
    }
 ...
}
```

- 若通过CAS设置变量State（同步状态）成功，也就是获取锁成功，则将当前线程设置为独占线程。

- 若通过CAS设置变量State（同步状态）失败，也就是获取锁失败，则进入Acquire方法进行后续处理。

- 看一下这个Acquire是怎么写的：

  ```java
  // java.util.concurrent.locks.AbstractQueuedSynchronizer
  
  public final void acquire(int arg) {
      if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
          selfInterrupt();
  }
  
  ```

  再看一下tryAcquire方法：

  ```java
  // java.util.concurrent.locks.AbstractQueuedSynchronizer
  
  protected boolean tryAcquire(int arg) {
       throw new UnsupportedOperationException();
  ```

具体获取锁的实现方法是由各自的公平锁和非公平锁单独实现的（以ReentrantLock为例）。如果该方法返回了True，则说明当前线程获取锁成功，就不用往后执行了；如果获取失败，就需要加入到等待队列中。

### 与synchronized比较

**1. 锁的实现**

synchronized 是 JVM 实现的，而 ReentrantLock 是 JDK 实现的。

**2. 性能**

新版本 Java 对 synchronized 进行了很多优化，例如自旋锁等，synchronized 与 ReentrantLock 大致相同。

**3. 等待可中断**

当持有锁的线程长期不释放锁的时候，正在等待的线程可以选择放弃等待，改为处理其他事情。

ReentrantLock 可中断，而 synchronized 不行。

**4. 公平锁**

公平锁是指多个线程在等待同一个锁时，必须按照申请锁的时间顺序来依次获得锁。

synchronized 中的锁是非公平的，ReentrantLock 默认情况下也是非公平的，但是也可以是公平的。

**5. 锁绑定多个条件**

一个 ReentrantLock 可以同时绑定多个 Condition 对象。

#### 使用选择

除非需要使用 ReentrantLock 的高级功能，否则优先使用 synchronized。这是因为 synchronized 是 JVM 实现的一种锁机制，JVM 原生地支持它，而 ReentrantLock 不是所有的 JDK 版本都支持。并且使用 synchronized 不用担心没有释放锁而导致死锁问题，因为 JVM 会确保锁的释放。

参考文章：从ReentrantLock的实现看AQS的原理及应用：https://mp.weixin.qq.com/s/sA01gxC4EbgypCsQt5pVog