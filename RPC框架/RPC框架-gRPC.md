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
   
   option java_package = "com.nation.grpc.lib";
   
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

配置文件

```xml
<dependencies>
<dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-stub</artifactId>
            <version>${grpc.version}</version>
            <exclusions>
                <exclusion>
                    <groupId>com.google.guava</groupId>
                    <artifactId>guava</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-protobuf</artifactId>
            <version>${grpc.version}</version>
            <exclusions>
                <exclusion>
                    <groupId>com.google.guava</groupId>
                    <artifactId>guava</artifactId>
                </exclusion>
            </exclusions>

        </dependency>

        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>20.0</version>
        </dependency>
        <dependency>
            <!-- Java 9+ compatibility -->
            <groupId>javax.annotation</groupId>
            <artifactId>javax.annotation-api</artifactId>
        </dependency>
  </dependencies>
<build>
    <extensions>
        <extension>
            <groupId>kr.motd.maven</groupId>
            <artifactId>os-maven-plugin</artifactId>
            <version>1.6.2</version>
        </extension>
    </extensions>
    <plugins>
        <plugin>
            <groupId>org.xolstice.maven.plugins</groupId>
            <artifactId>protobuf-maven-plugin</artifactId>
            <version>0.6.1</version>
            <configuration>
                <protocArtifact>com.google.protobuf:protoc:3.12.0:exe:${os.detected.classifier}</protocArtifact>
                <pluginId>grpc-java</pluginId>
                <pluginArtifact>io.grpc:protoc-gen-grpc-java:1.30.0:exe:${os.detected.classifier}</pluginArtifact>
                <!--默认值-->
                <protoSourceRoot>${project.basedir}/src/main/proto</protoSourceRoot>
                <!--默认值-->
                <!--<outputDirectory>${project.build.directory}/generated-sources/protobuf/java</outputDirectory>-->
                <outputDirectory>${project.basedir}/src/main/java</outputDirectory>
                <!--设置是否在生成java文件之前清空outputDirectory的文件，默认值为true，设置为false时也会覆盖同名文件-->
                <clearOutputDirectory>false</clearOutputDirectory>
                <!--更多配置信息可以查看https://www.xolstice.org/protobuf-maven-plugin/compile-mojo.html-->
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                        <goal>compile-custom</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

参考：https://github.com/grpc/grpc-java

值得注意的是，由于这里GUAVA版本依赖可能存在问题，所以需要根据MAVEN依赖自行修改

2.根据proto文件生成 Grpc和outerc.class

使用idea的工具能够快捷生成

![image-20200525155301601](https://github.com/rainluacgq/java/blob/master/RPC框架/pic/image-20200525155301601.png)



二、服务端配置

```xml
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-server-spring-boot-starter</artifactId>
</dependency>
```

配置文件：

```yml
grpc:
  server:
    port: 9898
    #max-message-size: 20971520
    maxInboundMessageSize: 20971520
```

代码

```java
/**
 * @Desc:
 * @Date: 2020/6/16
 */
@GrpcService
public class GrpcServiceDemo  extends GreeterGrpc.GreeterImplBase{

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void sayHello(GreeterOuterClass.HelloRequest request, StreamObserver<GreeterOuterClass.HelloReply> responseObserver) {
        String message = "Hello " + request.getName() + request.getLog().length();
        final GreeterOuterClass.HelloReply.Builder replyBuilder = GreeterOuterClass.HelloReply.newBuilder().setMessage(message);
        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
        log.info("Returning " +message);
    }

}
```

问题1：

```
io.grpc.StatusRuntimeException: CANCELLED: HTTP/2 error code: CANCEL
Received Rst Stream
```

先看一下源码，maxInboundMessageSize默认参数是Integer.MAX_VALUE，修改Server配置参数maxInboundMessageSize：

```java
//设置参数方法
public void setMaxInboundMessageSize(final DataSize maxInboundMessageSize) {
        if (maxInboundMessageSize == null || maxInboundMessageSize.toBytes() >= 0) {
            this.maxInboundMessageSize = maxInboundMessageSize;
        } else if (maxInboundMessageSize.toBytes() == -1) {
            this.maxInboundMessageSize = DataSize.ofBytes(Integer.MAX_VALUE);
        } else {
            throw new IllegalArgumentException("Unsupported maxInboundMessageSize: " + maxInboundMessageSize);
        }
    }

@Data
@ConfigurationProperties("grpc.server")
@SuppressWarnings("javadoc")
public class GrpcServerProperties {
    //省略若干代码
    @DataSizeUnit(DataUnit.BYTES)
    private DataSize maxInboundMessageSize = null;

}
```

```yaml
grpc:
  server:
    port: 9898
    maxInboundMessageSize: 20971520
```

三、客户端

pom依赖

```xml
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-client-spring-boot-starter</artifactId>
</dependency>
```

配置：

```yml
grpc:
  client:
    local-grpc-server:
      address: 'static://127.0.0.1:9898'
      enableKeepAlive: true
      keepAliveWithoutCalls: true
      negotiationType: plaintext ##如果没有该配置，会检查TLS，也会报错
      maxInBoundMessageSize: 20971520
```

代码

```java
/**
 * @Desc:
 * @Date: 2020/6/16
 */
@Service
public class GrpcTestService {
    @GrpcClient("local-grpc-server")
    private GreeterGrpc.GreeterBlockingStub myServiceStub;

    public String receiveGreeting(String name) throws IOException {
        GreeterOuterClass.HelloRequest request = GreeterOuterClass.HelloRequest.newBuilder()
                .setName(log)
                .build();
        return myServiceStub.sayHello(request).getMessage();
    }
}
```

看一下为啥这么配置

```java
private NegotiationType negotiationType;
private static final NegotiationType DEFAULT_NEGOTIATION_TYPE = NegotiationType.TLS;
/* 以下是NegotiationType的含义*/
public enum NegotiationType {

    /**
     * Uses TLS ALPN/NPN negotiation, assumes an SSL connection.
     */
    TLS,

    /**
     * Use the HTTP UPGRADE protocol for a plaintext (non-SSL) upgrade from HTTP/1.1 to HTTP/2.
     */
    PLAINTEXT_UPGRADE,

    /**
     * Just assume the connection is plaintext (non-SSL) and the remote endpoint supports HTTP/2 directly without an
     * upgrade.
     */
    PLAINTEXT;

}
```

参考：

http://doc.oschina.net/grpc?t=58010

https://www.jianshu.com/p/c7d390efba29



