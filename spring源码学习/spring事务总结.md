### Spring事务总结

####  一、概念一览

 MySQL 中，恢复机制是通过 **回滚日志（undo log）** 实现的，所有事务进行的修改都会先先记录到这个回滚日志中，然后再执行相关的操作。如果执行过程中遇到异常的话，我们直接利用 **回滚日志** 中的信息将数据回滚到修改之前的样子即可！并且，回滚日志会先于数据持久化到磁盘上。这样就保证了即使遇到数据库突然宕机等情况，当用户再次启动数据库的时候，数据库还能够通过查询回滚日志来回滚将之前未完成的事务。

####  二、Spring对事务的支持

#### 事务使用

**编程式事务**

```java
@Autowired
private PlatformTransactionManager transactionManager;

public void testTransaction() {

 TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
     try {
        *// .... 业务代码*
       transactionManager.commit(status);
     } catch (Exception e) {
       transactionManager.rollback(status);
     }
}
```

**声明式事务**

```java
@Transactional
public void aMethod {
 *//do something*
 c.cMethod();
}
```

Spring 框架中，事务管理相关最重要的 3 个接口如下：

- **`PlatformTransactionManager`**： （平台）事务管理器，Spring 事务策略的核心。
- **`TransactionDefinition`**： 事务定义信息(事务隔离级别、传播行为、超时、只读、回滚规则)。
- **`TransactionStatus`**： 事务运行状态。

我们可以把 **`PlatformTransactionManager`** 接口可以被看作是事务上层的管理者，而 **`TransactionDefinition`** 和 **`TransactionStatus`** 这两个接口可以看作是事物的描述。

**`PlatformTransactionManager`** 会根据 **`TransactionDefinition`** 的定义比如事务超时时间、隔离界别、传播行为等来进行事务管理 ，而**`TransactionStatus`** 接口则提供了一些方法来获取事务相应的状态比如是否新事务、是否可以回滚等等。

**Spring 并不直接管理事务，而是提供了多种事务管理器** 。Spring 事务管理器的接口是： **`PlatformTransactionManager`** 。

通过这个接口，Spring 为各个平台如 JDBC(`DataSourceTransactionManager`)、Hibernate(`HibernateTransactionManager`)、JPA(`JpaTransactionManager`)等都提供了对应的事务管理器，但是具体的实现就是各个平台自己的事情了

![image-20200802104730547](C:\Users\19349\AppData\Roaming\Typora\typora-user-images\image-20200802104730547.png)



PlatformTransactionManager定义了3个接口：

```java
public interface PlatformTransactionManager {
//获得事务
    TransactionStatus getTransaction(@Nullable TransactionDefinition var1) throws TransactionException;
//提交事务
    void commit(TransactionStatus var1) throws TransactionException;
//回滚
    void rollback(TransactionStatus var1) throws TransactionException;
}
```

#### TransactionDefinition:事务属性

事务管理器接口 **`PlatformTransactionManager`** 通过 **`getTransaction(TransactionDefinition definition)`** 方法来得到一个事务，这个方法里面的参数是 **`TransactionDefinition`** 类 ，这个类就定义了一些基本的事务属性。

```java
package org.springframework.transaction;

import org.springframework.lang.Nullable;

public interface TransactionDefinition {
    int PROPAGATION_REQUIRED = 0;
    int PROPAGATION_SUPPORTS = 1;
    int PROPAGATION_MANDATORY = 2;
    int PROPAGATION_REQUIRES_NEW = 3;
    int PROPAGATION_NOT_SUPPORTED = 4;
    int PROPAGATION_NEVER = 5;
    int PROPAGATION_NESTED = 6;
    int ISOLATION_DEFAULT = -1;
    int ISOLATION_READ_UNCOMMITTED = 1;
    int ISOLATION_READ_COMMITTED = 2;
    int ISOLATION_REPEATABLE_READ = 4;
    int ISOLATION_SERIALIZABLE = 8;
    int TIMEOUT_DEFAULT = -1;
 // 返回事务的传播行为，默认值为 REQUIRED。
    int getPropagationBehavior();
 //返回事务的隔离级别，默认值是 DEFAULT
    int getIsolationLevel();
  // 返回事务的超时时间，默认值为-1。如果超过该时间限制但事务还没有完成，则自动回滚事务。
    int getTimeout();
  // 返回是否为只读事务，默认值为 false
    boolean isReadOnly();

    @Nullable
    String getName();
}
```

`TransactionStatus`接口用来记录事务的状态 该接口定义了一组方法,用来获取或判断事务的相应状态信息。

```java
public interface TransactionStatus{
    boolean isNewTransaction(); // 是否是新的事物
    boolean hasSavepoint(); // 是否有恢复点
    void setRollbackOnly();  // 设置为只回滚
    boolean isRollbackOnly(); // 是否为只回滚
    boolean isCompleted; // 是否已完成
}

```



#### 事务传播级别

spring默认支持7种事务传播级别，其中最常用的是PROPAGATION_REQUIRED ，PROPAGATION_REQUIRES_NEW

```java
package org.springframework.transaction.annotation;

import org.springframework.transaction.TransactionDefinition;

public enum Propagation {

 REQUIRED(TransactionDefinition.PROPAGATION_REQUIRED),

 SUPPORTS(TransactionDefinition.PROPAGATION_SUPPORTS),

 MANDATORY(TransactionDefinition.PROPAGATION_MANDATORY),

 REQUIRES_NEW(TransactionDefinition.PROPAGATION_REQUIRES_NEW),

 NOT_SUPPORTED(TransactionDefinition.PROPAGATION_NOT_SUPPORTED),

 NEVER(TransactionDefinition.PROPAGATION_NEVER),

 NESTED(TransactionDefinition.PROPAGATION_NESTED);


 private final int value;

 Propagation(int value) {
 this.value = value;
 }

 public int value() {
 return this.value;
 }

}
```

**`TransactionDefinition.PROPAGATION_REQUIRED`**

使用的最多的一个事务传播行为，我们平时经常使用的`@Transactional`注解默认使用就是这个事务传播行为。如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务。也就是说：

1. 如果外部方法没有开启事务的话，`Propagation.REQUIRED`修饰的内部方法会新开启自己的事务，且开启的事务相互独立，互不干扰。
2. 如果外部方法开启事务并且被`Propagation.REQUIRED`的话，所有`Propagation.REQUIRED`修饰的内部方法和外部方法均属于同一事务 ，只要一个方法回滚，整个事务均回滚。

**`TransactionDefinition.PROPAGATION_REQUIRES_NEW`**

创建一个新的事务，如果当前存在事务，则把当前事务挂起。也就是说不管外部方法是否开启事务，`Propagation.REQUIRES_NEW`修饰的内部方法会新开启自己的事务，且开启的事务相互独立，互不干扰。

### @Transactional 注解使用详解

#### 1) `@Transactional` 的作用范围

1. **方法** ：推荐将注解使用于方法上，不过需要注意的是：**该注解只能应用到 public 方法上，否则不生效。**

```java
protected TransactionAttribute computeTransactionAttribute(Method method,
    Class<?> targetClass) {
        // Don't allow no-public methods as required.
        if (allowPublicMethodsOnly() && !Modifier.isPublic(method.getModifiers())) {
return null;}
```

2.**类** ：如果这个注解使用在类上的话，表明该注解对该类中所有的 public 方法都生效。

3.**接口** ：不推荐在接口上使用。因为一旦标注在Interface上并且配置了Spring AOP 使用CGLib动态代理，将会导致`@Transactional`注解失效

#### 2) `@Transactional` 的常用配置参数

`@Transactional`注解源码如下，里面包含了基本事务属性的配置：

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Transactional {

 @AliasFor("transactionManager")
 String value() default "";

 @AliasFor("value")
 String transactionManager() default "";

 Propagation propagation() default Propagation.REQUIRED;

 Isolation isolation() default Isolation.DEFAULT;

 int timeout() default TransactionDefinition.TIMEOUT_DEFAULT;

 boolean readOnly() default false;

 Class<? extends Throwable>[] rollbackFor() default {};

 String[] rollbackForClassName() default {};

 Class<? extends Throwable>[] noRollbackFor() default {};

 String[] noRollbackForClassName() default {};

}
```

**`@Transactional` 的常用配置参数总结（只列巨额 5 个我平时比较常用的）：**

| 属性名      | 说明                                                         |
| :---------- | :----------------------------------------------------------- |
| propagation | 事务的传播行为，默认值为 REQUIRED，可选的值在上面介绍过      |
| isolation   | 事务的隔离级别，默认值采用 DEFAULT，可选的值在上面介绍过     |
| timeout     | 事务的超时时间，默认值为-1（不会超时）。如果超过该时间限制但事务还没有完成，则自动回滚事务。 |
| readOnly    | 指定事务是否为只读事务，默认值为 false。                     |
| rollbackFor | 用于指定能够触发事务回滚的异常类型，并且可以指定多个异常类型。 |

参考：https://mp.weixin.qq.com/s/xFnVBqcVNRFQfHyd03iWcg



