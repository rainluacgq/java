### ReentrantLock总结

ReentrantLock意思为可重入锁，指的是一个线程能够对一个临界资源重复加锁。ReentrantLock虽然没能像Synchronized关键字一样支持隐式的重进入，但是调用lock方法时。已经获取到锁的线程，能够再次调用lock方法获取锁而不被阻塞。

![image-20200503182558940](https://github.com/rainluacgq/java/blob/master/JUC/pic/image-20200503182558940.png)

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

1.实现重进入

**线程再次获得锁**

锁需要识别获取锁的线程是否为当前占据锁的线程，如果不是，更新state和独占状态。如果是，则再次重新获取。

**锁的最终释放**

线程重复释放锁，其实就是对于state状态进行自减，当state == 0 时，表示锁已经成功释放。

```java
  final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
```

公平锁

唯一不同就是增加了hasQueuedPredecessors，即同步队列当前节点是否有前驱节点的判断，如果该方法返回true，表示有线程比当前线程更早获得锁。

```java

protected final boolean tryAcquire(int acquires) {
    final Thread current = Thread.currentThread();
    int c = getState();
    if (c == 0) {
        if (!hasQueuedPredecessors() &&
            compareAndSetState(0, acquires)) {
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    else if (current == getExclusiveOwnerThread()) {
        int nextc = c + acquires;
        if (nextc < 0)
            throw new Error("Maximum lock count exceeded");
        setState(nextc);
        return true;
    }
    return false;
}
```

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
