####  synchronized关键字总结

####  一、使用

```java
// 关键字在实例方法上，锁为当前实例
public synchronized void instanceLock() {
    // code
}

// 关键字在静态方法上，锁为当前Class对象
public static synchronized void classLock() {
    // code
}

// 关键字在代码块上，锁为括号里面的对象
public void blockLock() {
    Object o = new Object();
    synchronized (o) {
        // code
    }
}
```



###  二、锁的状态

Java 6 为了减少获得锁和释放锁带来的性能消耗，引入了“偏向锁”和“轻量级锁“。在Java 6 以前，所有的锁都是”重量级“锁。所以在Java 6 及其以后，一个对象其实有四种锁状态，它们级别由低到高依次是：

1. 无锁状态
2. 偏向锁状态
3. 轻量级锁状态
4. 重量级锁状态

##### 原理

每个Java对象都有对象头。如果是非数组类型，则用2个字宽来存储对象头，如果是数组，则会用3个字宽来存储对象头。在32位处理器中，一个字宽是32位；在64位虚拟机中，一个字宽是64位。对象头的内容如下表：

| 长度     | 内容                   | 说明                         |
| -------- | ---------------------- | ---------------------------- |
| 32/64bit | Mark Word              | 存储对象的hashCode或锁信息等 |
| 32/64bit | Class Metadata Address | 存储到对象类型数据的指针     |
| 32/64bit | Array length           | 数组的长度（如果是数组）     |

markword 64bit的含义如下：

![image-20200502165328528](https://github.com/rainluacgq/java/blob/master/%E5%A4%9A%E7%BA%BF%E7%A8%8B/pic/image-20200502165328528.png)



而在JDK源代码中，monitoroop源码如下所示：

```c
  ObjectMonitor() {
    _header       = NULL; //对象头，锁的状态
    _count        = 0;	//记录该线程获取锁的次数	
    _waiters      = 0,	//记录多少处于wait状态的thread
    _recursions   = 0;
    _object       = NULL;
    _owner        = NULL;	//指向持有objectMonttor的thread
    _WaitSet      = NULL;	//存放处于wait的线程队列
    _WaitSetLock  = 0 ;
    _Responsible  = NULL ;
    _succ         = NULL ;
    _cxq          = NULL ;
    FreeNext      = NULL ;
    _EntryList    = NULL ;		//存放处于阻塞状态的线程队列
    _SpinFreq     = 0 ;
    _SpinClock    = 0 ;
    OwnerIsThread = 0 ;
    _previous_owner_tid = 0;
  }
```

