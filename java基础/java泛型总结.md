### JAVA泛型

#### 一、概念

泛型提供了编译时类型安全检测机制，该机制允许程序员在编译时检测到非法的类型。泛型的本质是参数化类型，也就是说所操作的数据类型被指定为一个参数。 比如我们要写一个排序方法，能够对整型数组、字符串数组甚至其他任何类型的数组进行排序，我们就可以使用 Java 泛型。  

#### 二、实现：

1.泛型方法<E>

```java
// 泛型方法 printArray
public static < E > void printArray( E[] inputArray )
{
for ( E element : inputArray ){
System.out.printf( "%s ", element );
}
}  
```

2.泛型类

```java
public class Box<T> {
private T t;
public void add(T t) {
this.t = t;
}
public T get() {
return t;
}  
```

3.类型通配符

类 型 通 配 符 一 般 是 使 用 ? 代 替 具 体 的 类 型 参 数 。 例 如 List<?> 在 逻 辑 上 是
List<String>,List<Integer> 等所有 List<具体类型实参>的父类  

4.类型擦除

Java 中的泛型基本上都是在编译器这个层次来实现的。在生成的 Java 字节代码中是不包含泛型中的类型信息的。使用泛型的时候加上的类型参数，会被编译器在编译的时候去掉。这个过程就称为类型擦除。如在代码中定义的 List<Object>和 List<String>等类型，在编译之后都会变成 List。 JVM 看到的只是 List，而由泛型附加的类型信息对 JVM 来说是不可见的。类型擦除的基本过程也比较简单，首先是找到用来替换类型参数的具体类。这个具体类一般是 Object。如果指定了类型参数的上界的话，则使用这个上界。把代码中的类型参数都替换成具体的类。  

