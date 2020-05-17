### JAVA异常

#### 一、概念

![image-20200517155551184](C:\Users\19349\AppData\Roaming\Typora\typora-user-images\image-20200517155551184.png)

在Java中，异常就是Java在编译、运行或运行过程中出现的错误。

程序错误分为三种：编译错误、运行时错误和逻辑错误

- 编译错误是因为程序没有遵循语法规则，编译程序能够自己发现并且提示我们错误的原因和位置，这个也是新手在刚接触编程语言时经常遇到的问题。
- 运行时错误是因为程序在执行时，运行环境发现了不能执行的操作。
- 逻辑错误是因为程序没有按照预期的逻辑顺序执行。异常也就是指程序运行时发生错误，而异常处理就是对这些错误进行处理和控制。

####   二、异常分类
Throwable 是 Java 语言中所有错误或异常的超类。下一层分为 Error 和 Exception
Error

Error 类是指 java 运行时系统的内部错误和资源耗尽错误。应用程序不会抛出该类对象。如果
出现了这样的错误，除了告知用户，剩下的就是尽力使程序安全的终止。
Exception（RuntimeException、 CheckedException）
 Exception 又 有 两 个 分 支 ， 一 个 是 运 行 时 异 常 RuntimeException ， 一 个 是
CheckedException。
RuntimeException 如 ： NullPointerException 、 ClassCastException ； 一 个 是 检 查 异 常
CheckedException，如 I/O 错误导致的 IOException、 SQLException。 RuntimeException 是
那些可能在 Java 虚拟机正常运行期间抛出的异常的超类。 如果出现 RuntimeException，那么一
定是程序员的错误.
检查异常 CheckedException： 一般是外部错误，这种异常都发生在编译阶段， Java 编译器会强
制程序去捕获此类异常，即会出现要求你把这段可能出现异常的程序进行 try catch，该类异常一
般包括几个方面：

1. 试图在文件尾部读取数据
2. 试图打开一个错误格式的 URL
3. 试图根据给定的字符串查找 class 对象，而这个字符串表示的类并不存在

#####  异常的处理方式

遇到问题不进行具体处理，而是继续抛给调用者 （throw,throws）
抛出异常有三种形式，一是 throw,一个 throws，还有一种系统自动抛异常。

```java
public static void main(String[] args) {
String s = "abc";
if(s.equals("abc")) {
throw new NumberFormatException();
} else {
System.out.println(s);
}
}
int div(int a,int b) throws Exception{
return a/b;}
```

try catch 捕获异常针对性处理方式

#####  Throw 和 throws 的区别：
位置不同

1. throws 用在函数上，后面跟的是异常类，可以跟多个； 而 throw 用在函数内，后面跟的
   是异常对象。
   功能不同：
2. throws 用来声明异常，让调用者只知道该功能可能出现的问题，可以给出预先的处理方
   式； throw 抛出具体的问题对象，执行到 throw，功能就已经结束了，跳转到调用者，并
   将具体的问题对象抛给调用者。也就是说 throw 语句独立存在时，下面不要定义其他语
   句，因为执行不到。
3. throws 表示出现异常的一种可能性，并不一定会发生这些异常； throw 则是抛出了异常，
   执行 throw 则一定抛出了某种异常对象。

4. 两者都是消极处理异常的方式，只是抛出或者可能抛出异常，但是不会由函数去处理异
   常，真正的处理异常由函数的上层调用处理。  

try catch 捕获异常针对性处理方式  