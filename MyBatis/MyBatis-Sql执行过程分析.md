MyBatis执行过程分析

![image-20200619214116787](C:\Users\19349\AppData\Roaming\Typora\typora-user-images\image-20200619214116787.png)

MyBatis 层级结构各个组件的介绍：

- `SqlSession`： ，它是 MyBatis 核心 API，主要用来执行命令，获取映射，管理事务。接收开发人员提供 Statement Id 和参数。并返回操作结果。
- `Executor` ：执行器，是 MyBatis 调度的核心，负责 SQL 语句的生成以及查询缓存的维护。
- `StatementHandler` :  封装了JDBC Statement 操作，负责对 JDBC Statement 的操作，如设置参数、将Statement 结果集转换成 List 集合。
- `ParameterHandler` :  负责对用户传递的参数转换成 JDBC Statement 所需要的参数。
- `ResultSetHandler` : 负责将 JDBC 返回的 ResultSet 结果集对象转换成 List 类型的集合。
- `TypeHandler` :  用于 Java 类型和 JDBC 类型之间的转换。
- `MappedStatement` : 动态 SQL 的封装
- `SqlSource` :  表示从 XML 文件或注释读取的映射语句的内容，它创建将从用户接收的输入参数传递给数据库的 SQL。
- `Configuration`:  MyBatis 所有的配置信息都维持在 Configuration 对象之中。

###  SqlSessionFactory

对于任何框架而言，在使用该框架之前都要经历过一系列的初始化流程，其中最重要的就是SqlSessionFactory



```java
public interface SqlSessionFactory {

  SqlSession openSession();

  SqlSession openSession(boolean autoCommit);
  SqlSession openSession(Connection connection);
  SqlSession openSession(TransactionIsolationLevel level);

  SqlSession openSession(ExecutorType execType);
  SqlSession openSession(ExecutorType execType, boolean autoCommit);
  SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level);
  SqlSession openSession(ExecutorType execType, Connection connection);

  Configuration getConfiguration();

}
```

### sqlSession

在 MyBatis 初始化流程结束，也就是 SqlSessionFactoryBuilder -> SqlSessionFactory 的获取流程后，我们就可以通过 SqlSessionFactory 对象得到 `SqlSession` 然后执行 SQL 语句了。具体来看一下这个过程

在 SqlSessionFactory.openSession 过程中我们可以看到，会调用到 DefaultSqlSessionFactory 中的 `openSessionFromDataSource` 方法，这个方法主要创建了两个与我们分析执行流程重要的对象，一个是 `Executor` 执行器对象，一个是 `SqlSession` 对象。执行器我们下面会说，现在来说一下 SqlSession 对象

SqlSession 对象是 MyBatis 中最重要的一个对象，这个接口能够让你执行命令，获取映射，管理事务。SqlSession 中定义了一系列模版方法，让你能够执行简单的 `CRUD` 操作，也可以通过 `getMapper` 获取 Mapper 层，执行自定义 SQL 语句，因为 SqlSession 在执行 SQL 语句之前是需要先开启一个会话，涉及到事务操作，所以还会有 `commit`、 `rollback`、`close` 等方法。这也是模版设计模式的一种应用。

```java
private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
  Transaction tx = null;
  try {
    final Environment environment = configuration.getEnvironment();
    final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
    tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
    final Executor executor = configuration.newExecutor(tx, execType); //拿到执行器
    return new DefaultSqlSession(configuration, executor, autoCommit);
  } catch (Exception e) {
    closeTransaction(tx); // may have fetched a connection so lets call close()
    throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
  } finally {
    ErrorContext.instance().reset();
  }
}
```



### MapperProxy

MapperProxy 是 Mapper 映射 SQL 语句的关键对象，我们写的 Dao 层或者 Mapper 层都是通过 `MapperProxy` 来和对应的 SQL 语句进行绑定的。下面我们就来解释一下绑定过程

![20200619220419](C:\Users\19349\Desktop\20200619220419.png)

MapperProxyFactory 会生成代理对象，这个对象就是 MapperProxy，最终会调用到 mapperMethod.execute 方法，`execute` 方法比较长，其实逻辑比较简单，就是判断是 `插入`、`更新`、`删除` 还是 `查询` 语句

![image-20200619220616458](C:\Users\19349\AppData\Roaming\Typora\typora-user-images\image-20200619220616458.png)



### Executor

还记得我们之前的流程中提到了 `Executor(执行器)` 这个概念吗？我们来回顾一下它第一次出现的位置。



![img](https://user-gold-cdn.xitu.io/2020/2/1/16fff3cd171e7b30?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)



由 Configuration 对象创建了一个 `Executor` 对象，这个 Executor 是干嘛的呢？下面我们就来认识一下

#### Executor 的继承结构

每一个 SqlSession 都会拥有一个 Executor 对象，这个对象负责增删改查的具体操作，我们可以简单的将它理解为 JDBC 中 Statement 的封装版。 也可以理解为 SQL 的执行引擎，要干活总得有一个发起人吧，可以把 Executor 理解为发起人的角色。

首先先从 Executor 的继承体系来认识一下

![image-20200619221140503](C:\Users\19349\AppData\Roaming\Typora\typora-user-images\image-20200619221140503.png)



如上图所示，位于继承体系最顶层的是 Executor 执行器，它有两个实现类，分别是`BaseExecutor`和 `CachingExecutor`。

`BaseExecutor` 是一个抽象类，这种通过抽象的实现接口的方式是`适配器设计模式之接口适配` 的体现，是Executor 的默认实现，实现了大部分 Executor 接口定义的功能，降低了接口实现的难度。BaseExecutor 的子类有三个，分别是 SimpleExecutor、ReuseExecutor 和 BatchExecutor。

`SimpleExecutor` : 简单执行器，是 MyBatis 中**默认使用**的执行器，每执行一次 update 或 select，就开启一个Statement 对象，用完就直接关闭 Statement 对象(可以是 Statement 或者是 PreparedStatment 对象)

`ReuseExecutor` : 可重用执行器，这里的重用指的是重复使用 Statement，它会在内部使用一个 Map 把创建的Statement 都缓存起来，每次执行 SQL 命令的时候，都会去判断是否存在基于该 SQL 的 Statement 对象，如果存在 Statement 对象并且对应的 connection 还没有关闭的情况下就继续使用之前的 Statement 对象，并将其缓存起来。因为每一个 SqlSession 都有一个新的 Executor 对象，所以我们缓存在 ReuseExecutor 上的 Statement作用域是同一个 SqlSession。

`BatchExecutor` : 批处理执行器，用于将多个 SQL 一次性输出到数据库

`CachingExecutor`: 缓存执行器，先从缓存中查询结果，如果存在就返回之前的结果；如果不存在，再委托给Executor delegate 去数据库中取，delegate 可以是上面任何一个执行器。

#### Executor 的创建和选择

我们上面提到 `Executor` 是由 Configuration 创建的，Configuration 会根据执行器的类型创建，如下

```java
public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
  executorType = executorType == null ? defaultExecutorType : executorType;
  executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
  Executor executor;
  if (ExecutorType.BATCH == executorType) {
    executor = new BatchExecutor(this, transaction);
  } else if (ExecutorType.REUSE == executorType) {
    executor = new ReuseExecutor(this, transaction);
  } else {
    executor = new SimpleExecutor(this, transaction);
  }
  if (cacheEnabled) {
    executor = new CachingExecutor(executor);
  }
  executor = (Executor) interceptorChain.pluginAll(executor);
  return executor;
}
```

### StatementHandler

`StatementHandler` 是四大组件中最重要的一个对象，负责操作 Statement 对象与数据库进行交互，在工作时还会使用 `ParameterHandler` 和 `ResultSetHandler`对参数进行映射，对结果进行实体类的绑定

![img](https://user-gold-cdn.xitu.io/2020/2/1/16fff3cd7dc475c2?imageslim)

#### ParameterHandler 介绍

`ParameterHandler` 相比于其他的组件就简单很多了，ParameterHandler 译为参数处理器，负责为 PreparedStatement 的 sql 语句参数动态赋值，这个接口很简单只有两个方法



![img](https://user-gold-cdn.xitu.io/2020/2/1/16fff3cdb92821de?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)



ParameterHandler 只有一个实现类 `DefaultParameterHandler` ， 它实现了这两个方法。

- getParameterObject： 用于读取参数

#### ResultSetHandler 简介

ResultSetHandler 也是一个非常简单的接口



![img](https://user-gold-cdn.xitu.io/2020/2/1/16fff3cdbb635bcd?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)



ResultSetHandler 是一个接口，它只有一个默认的实现类，像是 ParameterHandler 一样，它的默认实现类是`DefaultResultSetHandler`

#### ResultSetHandler 解析过程

MyBatis 只有一个默认的实现类就是 `DefaultResultSetHandler`，DefaultResultSetHandler 主要负责处理两件事

- 处理 Statement 执行后产生的结果集，生成结果列表
- 处理存储过程执行后的输出参数

按照 Mapper 文件中配置的 ResultType 或 ResultMap 来封装成对应的对象，最后将封装的对象返回即可。

```java
public List<Object> handleResultSets(Statement stmt) throws SQLException {
  ErrorContext.instance().activity("handling results").object(mappedStatement.getId());

  final List<Object> multipleResults = new ArrayList<Object>();

  int resultSetCount = 0;
  // 获取第一个结果集
  ResultSetWrapper rsw = getFirstResultSet(stmt);
  // 获取结果映射
  List<ResultMap> resultMaps = mappedStatement.getResultMaps();
  // 结果映射的大小
  int resultMapCount = resultMaps.size();
  // 校验结果映射的数量
  validateResultMapsCount(rsw, resultMapCount);
  // 如果ResultSet 包装器不是null， 并且 resultmap 的数量  >  resultSet 的数量的话
  // 因为 resultSetCount 第一次肯定是0，所以直接判断 ResultSetWrapper 是否为 0 即可
  while (rsw != null && resultMapCount > resultSetCount) {
    // 从 resultMap 中取出 resultSet 数量
    ResultMap resultMap = resultMaps.get(resultSetCount);
    // 处理结果集, 关闭结果集
    handleResultSet(rsw, resultMap, multipleResults, null);
    rsw = getNextResultSet(stmt);
    cleanUpAfterHandlingResultSet();
    resultSetCount++;
  }

  // 从 mappedStatement 取出结果集
  String[] resultSets = mappedStatement.getResultSets();
  if (resultSets != null) {
    while (rsw != null && resultSetCount < resultSets.length) {
      ResultMapping parentMapping = nextResultMaps.get(resultSets[resultSetCount]);
      if (parentMapping != null) {
        String nestedResultMapId = parentMapping.getNestedResultMapId();
        ResultMap resultMap = configuration.getResultMap(nestedResultMapId);
        handleResultSet(rsw, resultMap, null, parentMapping);
      }
      rsw = getNextResultSet(stmt);
      cleanUpAfterHandlingResultSet();
      resultSetCount++;
    }
  }

  return collapseSingleResultList(multipleResults);
}
```

其中涉及的主要对象有：

`ResultSetWrapper` : 结果集的包装器，主要针对结果集进行的一层包装，它的主要属性有

- `ResultSet` : Java JDBC ResultSet 接口表示数据库查询的结果。 有关查询的文本显示了如何将查询结果作为java.sql.ResultSet 返回。 然后迭代此ResultSet以检查结果。
- `TypeHandlerRegistry`: 类型注册器，TypeHandlerRegistry 在初始化的时候会把所有的 Java类型和类型转换器进行注册。
- `ColumnNames`: 字段的名称，也就是查询操作需要返回的字段名称
- `ClassNames`: 字段的类型名称，也就是 ColumnNames 每个字段名称的类型
- `JdbcTypes`:  JDBC 的类型，也就是 java.sql.Types 类型
- `ResultMap`: 负责处理更复杂的映射关系

在 DefaultResultSetHandler 中处理完结果映射，并把上述结构返回给调用的客户端，从而执行完成一条完整的SQL语句。


参考：https://juejin.im/post/5e350d895188254dfd43def5
