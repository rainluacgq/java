### java创建对象的5种方式

一、使用new关键字

二、使用clone方法

三、使用反序列化

四、使用反射

**使用Class.newInstance()创建对象**

**调用类对象的构造方法——Constructor**



五、使用unsafe

sun.misc.Unsafe中提供`allocateInstance`方法，仅通过Class对象就可以创建此类的实例对象，而且不需要调用其构造函数、初始化代码、JVM安全检查等。它抑制修饰符检测，也就是即使构造器是private修饰的也能通过此方法实例化，只需提类对象即可创建相应的对象。由于这种特性，allocateInstance在java.lang.invoke、Objenesis（提供绕过类构造器的对象生成方式）、Gson（反序列化时用到）中都有相应的应用。

```java
package cn.eft.llj.unsafe;
import java.lang.reflect.Field;

import sun.misc.Unsafe;
public class Demo9 {
static Unsafe unsafe;

static {
    //获取Unsafe对象
    try {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        unsafe = (Unsafe) field.get(null);
    } catch (Exception e) {
        e.printStackTrace();
    }
}

static class C1 {
    private String name;

    private C1() {
        System.out.println("C1 default constructor!");
    }

    private C1(String name) {
        this.name = name;
        System.out.println("C1 有参 constructor!");
    }
    
    public void test(){
        System.out.println("执行了test方法");
    }
}

public static void main(String[] args) throws InstantiationException {
    C1 c= (C1) unsafe.allocateInstance(C1.class);
    System.out.println(c);
    c.test();
}
```
}

参考：https://juejin.im/post/5d44530a6fb9a06aed7103bd
