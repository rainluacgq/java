### JAVA反射

#### 一、概念

Java 反射机制：在运行过程中，对于任意一个类，都能知道其所有的属性和方法；对于任意一个对象，都能调用其属性和方法；这种动态获取类信息和调用对象方法的功能，就是 Java 反射机制。

既然反射里面有一个“反”字，那么我们先看看何为“正”。

在 Java 中，要使用一个类中的某个方法，“正向”都是这样的：

```java
ArrayList list = new ArrayList(); //实例化list.add("reflection");  //执行方法
```

那么反向（反射）要如何实现？

```java
Class clz = Class.forName("java.util.ArrayList");
Method method_add = clz.getMethod("add",Object.class);
Constructor constructor = clz.getConstructor();
Object object = constructor.newInstance();
method_add.invoke(object, "reflection");
Method method_get = clz.getMethod("get",int.class);
System.out.println(method_get.invoke(object, 0));
```

#### 二、关键实现

Class 和 java.lang.reflect 一起对反射提供了支持，java.lang.reflect 类库主要包含了以下三个类：
**Field** ：可以使用 get() 和 set() 方法读取和修改 Field 对象关联的字段；
**Method** ：可以使用 invoke() 方法调用与 Method 对象关联的方法；
**Constructor** ：可以用 Constructor 创建新的对象。
**反射的优点：
可扩展性** ：应用程序可以利用全限定名创建可扩展对象的实例，来使用来自外部的用户自定义类。
**类浏览器和可视化开发环境** ：一个类浏览器需要可以枚举类的成员。可视化开发环境（如 IDE）可以从利用反
射中可用的类型信息中受益，以帮助程序员编写正确的代码。
**调试器和测试工具** ： 调试器需要能够检查一个类里的私有成员。测试工具可以利用反射来自动地调用类里定义
的可被发现的 API 定义，以确保一组测试中有较高的代码覆盖率。
**反射的缺点：**
尽管反射非常强大，但也不能滥用。如果一个功能可以不用反射完成，那么最好就不用。在我们使用反射技术时，下
面几条内容应该牢记于心。
**性能开销** ：反射涉及了动态类型的解析，所以 JVM 无法对这些代码进行优化。因此，反射操作的效率要比那些
非反射操作低得多。我们应该避免在经常被执行的代码或对性能要求很高的程序中使用反射。
**安全限制** ：使用反射技术要求程序必须在一个没有安全限制的环境中运行。如果一个程序必须在有安全限制的
环境中运行，如 Applet，那么这就是个问题了。
**内部暴露** ：由于反射允许代码执行一些在正常情况下不被允许的操作（比如访问私有的属性和方法），所以使
用反射可能会导致意料之外的副作用，这可能导致代码功能失调并破坏可移植性。反射代码破坏了抽象性，因
此当平台发生改变的时候，代码的行为就有可能也随着变化  