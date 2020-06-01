### gPRC实战

### 1.概念

gRPC 基于如下思想：定义一个服务， 指定其可以被远程调用的方法及其参数和返回类型。gRPC 默认使用 [protocol buffers](https://developers.google.com/protocol-buffers/) 作为接口定义语言，来描述服务接口和有效载荷消息结构。如果有需要的话，可以使用其他替代方案。

gRPC使用protocol buffer作为序列化和通信的接口定义语言，而不是JSON/XML。Protocol buffer可以描述数据的结构，并且可以根据该描述生成代码，以生成或解析表示结构化数据的字节流。这就是为什么gRPC更适合使用polyglot（使用不同的技术部署）的Web应用程序。二进制数据格式使得通信更加轻量，gRPC也可以与其他数据格式一起使用，但首选的格式仍然是protocol buffer。

此外，gRPC构建在HTTP/2之上，它支持双向通信以及传统的请求/响应。gRPC允许服务器和客户端之间的松散耦合。在实践中，客户端发起一个与gRPC服务器的长连接，并为每个RPC调用打开一个新的HTTP/2流。

![image-20200601223141657](C:\Users\19349\AppData\Roaming\Typora\typora-user-images\image-20200601223141657.png)

**调用模型**

1、客户端（gRPC Stub）调用 A 方法，发起 RPC 调用。

2、对请求信息使用 Protobuf 进行对象序列化压缩（IDL）。

3、服务端（gRPC Server）接收到请求后，解码请求体，进行业务逻辑处理并返回。

4、对响应结果使用 Protobuf 进行对象序列化压缩（IDL）。

5、客户端接受到服务端响应，解码请求体。回调被调用的 A 方法，唤醒正在等待响应（阻塞）的客户端调用并返回响应结果。

------

**HTTP2.0有哪些新特性**

 参考：https://cloud.tencent.com/developer/article/1004874



### 2.常见的错误类型

当应用错误或运行时错误在 PRC 调用过程中出现时，**状态**和**状态消息**应当通过**跟踪消息**发送。

在有些情况下可能消息流的帧已经中断，RPC 运行时会选择使用 **RST_STREAM** 帧来给对方表示这种状态。RPC 运行时声明应当将 RST_STREAM 解释为流的完全关闭，并且将错误传播到应用层。 以下为从 RST_STREAM 错误码到 GRPC 的错误码的映射：

| HTTP2 编码            |                          GRPC 编码                           |
| :-------------------- | :----------------------------------------------------------: |
| NO_ERROR(0)           | INTERNAL -一个显式的GRPC OK状态应当被发出，但是这个也许在某些场景里会被侵略性地使用 |
| PROTOCOL_ERROR(1)     |                           INTERNAL                           |
| INTERNAL_ERROR(2)     |                           INTERNAL                           |
| FLOW_CONTROL_ERROR(3) |                           INTERNAL                           |
| SETTINGS_TIMEOUT(4)   |                           INTERNAL                           |
| STREAM_CLOSED         |         无映射，因为没有打开的流来传播。实现应记录。         |
| FRAME_SIZE_ERROR      |                           INTERNAL                           |
| REFUSED_STREAM        |   UNAVAILABLE-表示请求未作处理且可以重试，可能在他处重试。   |
| CANCEL(8)             | 当是由客户端发出时映射为调用取消，当是由服务端发出时映射为 CANCELLED。注意服务端在需要取消调用时应仅仅使用这个机制，但是有效荷载字节顺序是不完整的 |
| COMPRESSION_ERROR     |                           INTERNAL                           |
| CONNECT_ERROR         |                           INTERNAL                           |
| ENHANCE_YOUR_CALM     | RESOURCE_EXHAUSTED...并且运行时提供有额外的错误详情，表示耗尽资源是带宽 |
| INADEQUATE_SECURITY   | PERMISSION_DENIED... 并且有额外的信息表明许可被拒绝，因为对调用来说协议不够安全 |

### 2.实战

1.  编写proto文件

   ```protobuf
   syntax = "proto3"; //protocol buffer 协议版本
   
   option java_package = "com.linshen.grpc.lib";
   
   // The greeter service definition.
   service Greeter {
       // Sends a greeting
       rpc SayHello ( HelloRequest) returns (  HelloReply) {}
   
   }
   // The request message containing the user's name.
   message HelloRequest {
       string name = 1;
   }
   // The response message containing the greetings
   message HelloReply {
       string message = 1;
   }
   ```

2.根据proto文件生成 Grpc和outerc.class

使用idea的工具能够快捷生成

![image-20200525155301601](https://github.com/rainluacgq/java/blob/master/RPC框架/pic/image-20200525155301601.png)



问题1：

```
io.grpc.StatusRuntimeException: CANCELLED: HTTP/2 error code: CANCEL
Received Rst Stream
```

修改Server配置文件，GrpcServerProperties的源码如下所示：

```java
@Data
@ConfigurationProperties("grpc.server")
public class GrpcServerProperties {
    /**
     * Server port to listen on. Defaults to 9090.
     */
    private int port = 9090;

    /**
     * Bind address for the server. Defaults to 0.0.0.0.
     */
    private String address = "0.0.0.0";
    
    /**
     * The maximum message size allowed to be received for the server.
     */
    private int maxMessageSize;

    /**
     * Security options for transport security. Defaults to disabled. 
     */
    private final Security security = new Security();

    @Data
    public static class Security {

        /**
         * Flag that controls whether transport security is used
         */
        private Boolean enabled = false;

        /**
         * Path to SSL certificate chain
         */
        private String certificateChainPath = "";

        /**
         * Path to SSL certificate
         */
        private String certificatePath = "";

    }

    public int getPort() {
        if (this.port == 0) {
            this.port = SocketUtils.findAvailableTcpPort();
        }
        return this.port;
    }
}
```

修改application.xml文件如下所示：

```yaml
grpc:
  server:
    port: 9898
    maxMessageSize: 20971520 #表示允许的最大传输大小是20M（该数字大小是byte）
```



参考：

http://doc.oschina.net/grpc?t=58010

https://www.jianshu.com/p/c7d390efba29



