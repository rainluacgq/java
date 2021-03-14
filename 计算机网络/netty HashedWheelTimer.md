netty总结

###  一、框架概览

Netty 是一个高性能、异步事件驱动的 NIO 框架，基于 JAVA NIO 提供的 API 实现。它提供了对TCP、 UDP 和文件传输的支持，作为一个异步 NIO 框架， Netty 的所有 IO 操作都是异步非阻塞的， 通过 Future-Listener 机制，用户可以方便的主动获取或者通过通知机制获得 IO 操作结果。  

### 二、Netty 核心组件

在还未入门 Netty 之前我们先了解一下 Netty 里面都有哪些类，做到有的放矢，后面学习带着这些关键信息不回乱。

**①Bootstrap、ServerBootstrap**

一个 Netty 应用通常由一个 Bootstrap 开始，主要作用是配置整个 Netty 程序，串联各个组件，Netty 中 Bootstrap 类是客户端程序的启动引导类，ServerBootstrap 是服务端启动引导类。

**②Future、ChannelFuture**

在 Netty 中所有的 IO 操作都是异步的，不会立刻知道某个事件是否完成处理。

但是可以过一会等它执行完成或者直接注册一个监听，具体的实现就是通过 Future 和 ChannelFutures，用来注册一个监听，当操作执行成功或失败时监听会自动触发注册的监听事件。

**③Channel**

Netty 网络通信的组件，能够用于执行网络 I/O 操作。

Chanel 为用户提供：

- 当前网络连接的通道的状态（例如是否打开，是否已连接）。
- 网络连接的配置参数 （例如接收缓冲区大小）。
- 提供异步的网络 I/O 操作(如建立连接，读写，绑定端口)，异步调用意味着任何 I/O 调用都将立即返回，并且不保证在调用结束时所请求的 I/O 操作已完成。
- 调用立即返回一个 ChannelFuture 实例，通过注册监听器到 ChannelFuture 上，可以 I/O 操作成功、失败或取消时回调通知调用方。
- 支持关联 I/O 操作与对应的处理程序。

不同协议、不同的阻塞类型的连接都有不同的 Channel 类型与之对应。

下面是一些常用的 Channel 类型：

- **NioSocketChannel，**异步的客户端 TCP Socket 连接。
- **NioServerSocketChannel，**异步的服务器端 TCP Socket 连接。
- **NioDatagramChannel，**异步的 UDP 连接。
- **NioSctpChannel，**异步的客户端 Sctp 连接。
- **NioSctpServerChannel，**异步的 Sctp 服务器端连接，这些通道涵盖了 UDP 和 TCP 网络 IO 以及文件 IO。

**④Selector**

Netty 基于 Selector 对象实现 I/O 多路复用，通过 Selector 一个线程可以监听多个连接的 Channel 事件。

当向一个 Selector 中注册 Channel 后，Selector 内部的机制就可以自动不断地查询（Select）这些注册的 Channel 是否有已就绪的 I/O 事件（例如可读，可写，网络连接完成等），这样程序就可以很简单地使用一个线程高效地管理多个 Channel 。

**⑤NioEventLoop**

NioEventLoop 中维护了一个线程和任务队列，支持异步提交执行任务，线程启动时会调用 NioEventLoop 的 run 方法，执行 I/O 任务和非 I/O 任务：

- I/O 任务，即 selectionKey 中 ready 的事件，如 accept、connect、read、write 等，由 processSelectedKeys 方法触发。
- 非 IO 任务，添加到 taskQueue 中的任务，如 register0、bind0 等任务，由 runAllTasks 方法触发。

两种任务的执行时间比由变量 ioRatio 控制，默认为 50，则表示允许非 IO 任务执行的时间与 IO 任务的执行时间相等。

**⑥NioEventLoopGroup**

NioEventLoopGroup，主要管理 eventLoop 的生命周期，可以理解为一个线程池，内部维护了一组线程，每个线程（NioEventLoop）负责处理多个 Channel 上的事件，而一个 Channel 只对应于一个线程。

**⑦ChannelHandler**

ChannelHandler 是一个接口，处理 I/O 事件或拦截 I/O 操作，并将其转发到其 ChannelPipeline（业务处理链）中的下一个处理程序。

ChannelHandler 本身并没有提供很多方法，因为这个接口有许多的方法需要实现，方便使用期间，可以继承它的子类：

- ChannelInboundHandler 用于处理入站 I/O 事件。
- ChannelOutboundHandler 用于处理出站 I/O 操作。

或者使用以下适配器类：

- ChannelInboundHandlerAdapter 用于处理入站 I/O 事件。
- ChannelOutboundHandlerAdapter 用于处理出站 I/O 操作。
- ChannelDuplexHandler 用于处理入站和出站事件。

**⑧ChannelHandlerContext**

保存 Channel 相关的所有上下文信息，同时关联一个 ChannelHandler 对象。

**⑨ChannelPipline**

保存 ChannelHandler 的 List，用于处理或拦截 Channel 的入站事件和出站操作。

它实现了一种高级形式的拦截过滤器模式，使用户可以完全控制事件的处理方式，以及 Channel 中各个的 ChannelHandler 如何相互交互。

在 Netty 中每个 Channel 都有且仅有一个 ChannelPipeline 与之对应。

### 三、实战

### 1.TCP沾包分包

通过LengthFieldBasedFrameDecoder解码器实现：

```java
ph.addLast(new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN,65536,0,
        4,0,4,true));

public LengthFieldBasedFrameDecoder(
            ByteOrder byteOrder, int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
            int lengthAdjustment, int initialBytesToStrip, boolean failFast)
```

关键参数的含义：

1. maxFrameLength - 发送的数据帧最大长度

2. lengthFieldOffset - 定义长度域位于发送的字节数组中的下标。换句话说：发送的字节数组中下标为${lengthFieldOffset}的地方是长度域的开始地方

3. lengthFieldLength - 用于描述定义的长度域的长度。换句话说：发送字节数组bytes时, 字节数组bytes[lengthFieldOffset, lengthFieldOffset+lengthFieldLength]域对应于的定义长度域部分

4. lengthAdjustment - 满足公式: 发送的字节数组bytes.length - lengthFieldLength = bytes[lengthFieldOffset, lengthFieldOffset+lengthFieldLength] + lengthFieldOffset + lengthAdjustment 

5. initialBytesToStrip - 接收到的发送数据包，去除前initialBytesToStrip位

6. failFast - true: 读取到长度域超过maxFrameLength，就抛出一个 TooLongFrameException。false: 只有真正读取完长度域的值表示的字节之后，才会抛出 TooLongFrameException，默认情况下设置为true，建议不要修改，否则可能会造成内存溢
7. ByteOrder - 数据存储采用大端模式或小端模式

### 源码学习

1. Handler 业务处理器

   当Java NIO事件进站到Channel时，产生一的一系列事件将由ChannelHandler所对应的API处理。

![image-20200708163223148](https://github.com/rainluacgq/java/blob/master/计算机网络/pic/image-20200708163223148.png)

1. 注册事件 fireChannelRegistered。

2. 连接建立事件 fireChannelActive。

3. 读事件和读完成事件 fireChannelRead、fireChannelReadComplete。

4. 异常通知事件 fireExceptionCaught。

5. 用户自定义事件 fireUserEventTriggered。

6. Channel 可写状态变化事件 fireChannelWritabilityChanged。

7. 连接关闭事件 fireChannelInactive。

2.**ChannelOutboundHandler出站处理器**

![chanelOutBand](https://github.com/rainluacgq/java/blob/master/计算机网络/pic/chanelOutBand.jpg)

1. 端口绑定 bind。

2. 连接服务端 connect。

3. 写事件 write。

4. 刷新时间 flush。

5. 读事件 read。

6. 主动断开连接 disconnect。

7. 关闭 channel 事件 close。



- 参考：https://mp.weixin.qq.com/s/csslzxEGTRX1WnK5Qp8jWQ

