### java新特性

## Java 各版本的新特性

**New highlights in Java SE 8**

1. Lambda Expressions

   如不使用lambda表达式，创建一个线程通常:

   ```java
   new Thread(new Runnable() {
   	@Override
   	public void run() {
   		System.out.println("hello world");
   	}
   }).start();
   ```

   使用lambda表达式：

   ```java
   new Thread(() -> System.out.println("hello world")).start();
   ```

2. Pipelines and Streams

3. Date and Time API

   Java8中关于时间日期的API有以下关键点：

   - 提供了javax.time.ZoneId用来处理时区。
   - 提供了LocalDate与LocalTime类。
   - 时间与日期API中的所有类都是线程安全的。
   - 明确定义了基本的时间与日期概念。
   - 核心API：Instant、LocalDate、LocalTime、LocalDateTime、ZonedDateTime。
   - DateTimeFormatter类用于在Java中进行日期的格式化与解析。

   

4. Default Methods

5. Type Annotations

6. Nashhorn JavaScript Engine

7. Concurrent Accumulators

8. Parallel operations

9. PermGen Error Removed

   废除永久代、迎来元空间(metaspace)

**New highlights in Java SE 7**

1. Strings in Switch Statement
2. Type Inference for Generic Instance Creation
3. Multiple Exception Handling
4. Support for Dynamic Languages
5. Try with Resources
6. Java nio Package
7. Binary Literals, Underscore in literals
8. Diamond Syntax

参考链接：https://www.selfgrowth.com/articles/difference-between-java-18-and-java-17

参考文章：https://mp.weixin.qq.com/s/8uh-oo49erFwnVEY2emSQQ