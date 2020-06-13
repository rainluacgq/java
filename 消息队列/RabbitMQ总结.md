####  RabbitMQ总结

RabbitMQ是一个开源的AMQP实现，服务器端用 Erlang 语言编写，支持多种客户端，如：Python、Ruby、.NET、Java、JMS、C、PHP、ActionScript、XMPP、STOMP等，也支持AJAX。主要用于在分布式系统中存储和转发消息，具有很高的易用性和可用性。

AMQP，即 Advanced Message Queuing Protocol，高级消息队列协议，是应用层协议的一个开放标准，为面向消息的中间件设计的。消息中间件主要用于组件之间的解耦和通讯。AMQP的主要特征是面向消息、队列、路由（包括点对点和发布/订阅）、可靠性和安全。

官网：http://www.rabbitmq.com

二、RabbitMQ概念介绍



####  2.1 相关概念介绍

## 

## ** **

## **1、ConnectionFactory、Connection、Channel**

ConnectionFactory、Connection、Channel都是RabbitMQ对外提供的API中最基本的对象。



**Connection**是RabbitMQ的socket链接，它封装了socket协议相关部分逻辑。

**ConnectionFactory**是Connection的制造工厂。

**Channel**是我们与RabbitMQ打交道的最重要的一个接口，我们大部分的业务操作是在Channel这个接口中完成的，包括定义Queue、定义Exchange、绑定Queue与Exchange、发布消息等。



## **2、Queue**

Queue（队列）是RabbitMQ的内部对象，主要用于存储消息，用下图表示。

![img](https://mmbiz.qpic.cn/mmbiz_png/NIibUFNmgiawqYrmSGu8F369cvOO125qRrh6icic1cd6LunyawHiaUBucvvFmrG8AUYIzTABkZtqMJ2g4NgKYa8hNiaA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



RabbitMQ中的消息都只能存储在Queue中，生产者（下图中的P）生产消息并最终投递到Queue中，消费者（下图中的C）可以从Queue中获取消息并消费。

![img](https://mmbiz.qpic.cn/mmbiz_png/NIibUFNmgiawqYrmSGu8F369cvOO125qRr3TDGefa3jsWEKvBI4oSgu1yqdxfpWh3gI0RLjUnMCHYYDodpNQUcCQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



多个消费者可以订阅同一个Queue，这时Queue中的消息会被平均分摊给多个消费者进行处理，而不是每个消费者都收到所有的消息并处理。

![img](https://mmbiz.qpic.cn/mmbiz_png/NIibUFNmgiawqYrmSGu8F369cvOO125qRrIkA5kGiaxQ8LO73IKYBibMcdD3ZI59O40NAv61Zbyk7IF8sBboYNRm6A/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

##  

## **3、Message Acknowledgment**

在实际应用中，可能会发生消费者收到Queue中的消息，但没有处理完成就宕机（或出现其他意外）的情况，这种情况下就可能会导致消息丢失。为了避免这种情况发生，我们可以要求消费者在消费完消息后发送一个回执给RabbitMQ，RabbitMQ收到消息回执（Message acknowledgment）后才将该消息从Queue中移除；如果RabbitMQ没有收到回执并检测到消费者的RabbitMQ连接断开，则RabbitMQ会将该消息发送给其他消费者（如果存在多个消费者）进行处理。这里不存在timeout概念，一个消费者处理消息时间再长也不会导致该消息被发送给其他消费者，除非它的RabbitMQ连接断开。

这里会产生另外一个问题，如果我们的开发人员在处理完业务逻辑后，忘记发送回执给RabbitMQ，这将会导致严重的bug——Queue中堆积的消息会越来越多；消费者重启后会重复消费这些消息并重复执行业务逻辑。另外pub message是没有ack的。

## **4、Message Durability**

如果我们希望即使在RabbitMQ服务重启的情况下，也不会丢失消息，我们可以将Queue与Message都设置为可持久化的（durable），这样可以保证绝大部分情况下我们的RabbitMQ消息不会丢失。但依然解决不了小概率丢失事件的发生（比如RabbitMQ服务器已经接收到生产者的消息，但还没来得及持久化该消息时RabbitMQ服务器就断电了），如果我们需要对这种小概率事件也要管理起来，那么我们要用到事务。由于这里仅为RabbitMQ的简单介绍，所以这里将不讲解RabbitMQ相关的事务。

## **5、Prefetch Count**

前面我们讲到如果有多个消费者同时订阅同一个Queue中的消息，Queue中的消息会被平摊给多个消费者。这时如果每个消息的处理时间不同，就有可能会导致某些消费者一直在忙，而另外一些消费者很快就处理完手头工作并一直空闲的情况。我们可以通过设置prefetchCount来限制Queue每次发送给每个消费者的消息数，比如我们设置prefetchCount=1，则Queue每次给每个消费者发送一条消息；消费者处理完这条消息后Queue会再给该消费者发送一条消息。

![img](https://mmbiz.qpic.cn/mmbiz_png/NIibUFNmgiawqYrmSGu8F369cvOO125qRrjLibQxnCKCnQjcliaiboTHtDvia5HQtg8ledPQSGS3JCvNvaPUibgbrlziaQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

##  

## **6、Exchange**

在上一节我们看到生产者将消息投递到Queue中，实际上这在RabbitMQ中这种事情永远都不会发生。实际的情况是，生产者将消息发送到Exchange（交换器，下图中的X），由Exchange将消息路由到一个或多个Queue中（或者丢弃）。

![img](https://mmbiz.qpic.cn/mmbiz_png/NIibUFNmgiawqYrmSGu8F369cvOO125qRrLzYMNkkHndiao2ejXI3rxx3I8dqgChRj5f9hKNniclfcOzSXmgic3zkag/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



Exchange是按照什么逻辑将消息路由到Queue的？这个将在下面的**8、Binding**中介绍。

RabbitMQ中的Exchange有四种类型，不同的类型有着不同的路由策略，这将在下面的**10、Exchange Types**中介绍。

## **7、Routing Key**

生产者在将消息发送给Exchange的时候，一般会指定一个routing key，来指定这个消息的路由规则，而这个routing key需要与Exchange Type及binding key联合使用才能最终生效。

在Exchange Type与binding key固定的情况下（在正常使用时一般这些内容都是固定配置好的），我们的生产者就可以在发送消息给Exchange时，通过指定routing key来决定消息流向哪里。RabbitMQ为routing key设定的长度限制为255 bytes。

## **8、Binding**

RabbitMQ通过Binding将Exchange与Queue关联起来，这样RabbitMQ就知道如何正确地将消息路由到指定的Queue了。

![img](https://mmbiz.qpic.cn/mmbiz_png/NIibUFNmgiawqYrmSGu8F369cvOO125qRrbrLhrVfGODI4GlnVvEHT8pUcCockWpPCZMQLe7iccpnT1FpGPrPA4zg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

##  

## **9、Binding Key**

在绑定（Binding）Exchange与Queue的同时，一般会指定一个binding key；消费者将消息发送给Exchange时，一般会指定一个routing key；当binding key与routing key相匹配时，消息将会被路由到对应的Queue中。这个将在Exchange Types章节会列举实际的例子加以说明。

在绑定多个Queue到同一个Exchange的时候，这些Binding允许使用相同的binding key。
binding key 并不是在所有情况下都生效，它依赖于Exchange Type，比如fanout类型的Exchange就会无视binding key，而是将消息路由到所有绑定到该Exchange的Queue。

## **10、Exchange Types**

RabbitMQ常用的Exchange Type有fanout、direct、topic、headers这四种（AMQP规范里还提到两种Exchange Type，分别为system与自定义，这里不予以描述），下面分别进行介绍。

**fanout**

fanout类型的Exchange路由规则非常简单，它会把所有发送到该Exchange的消息路由到所有与它绑定的Queue中。

![img](https://mmbiz.qpic.cn/mmbiz_png/NIibUFNmgiawqYrmSGu8F369cvOO125qRrgRTGgvJviclAQH7libd12jaXIbe871FbZN76iaswtZGDuSE00CWkD3VVw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



上图中，生产者（P）发送到Exchange（X）的所有消息都会路由到图中的两个Queue，并最终被两个消费者（C1与C2）消费。

**direct**

direct类型的Exchange路由规则也很简单，它会把消息路由到那些binding key与routing key完全匹配的Queue中。

![img](https://mmbiz.qpic.cn/mmbiz_png/NIibUFNmgiawqYrmSGu8F369cvOO125qRrsKX16NibicGlSicpiciceRDSMtTCKaX0qvxXibslvYqDbe1o27PFFqmw1JxQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



以上图的配置为例，我们以routingKey=”error”发送消息到Exchange，则消息会路由到Queue1（amqp.gen-S9b…，这是由RabbitMQ自动生成的Queue名称）和Queue2（amqp.gen-Agl…）；如果我们以routingKey=”info”或routingKey=”warning”来发送消息，则消息只会路由到Queue2。如果我们以其他routingKey发送消息，则消息不会路由到这两个Queue中。

**topic**

前面讲到direct类型的Exchange路由规则是完全匹配binding key与routing key，但这种严格的匹配方式在很多情况下不能满足实际业务需求。topic类型的Exchange在匹配规则上进行了扩展，它与direct类型的Exchage相似，也是将消息路由到binding key与routing key相匹配的Queue中，但这里的匹配规则有些不同，它约定：

routing key为一个句点号“. ”分隔的字符串（我们将被句点号“. ”分隔开的每一段独立的字符串称为一个单词），如“stock.usd.nyse”、“nyse.vmw”、“quick.orange.rabbit”

binding key与routing key一样也是句点号“.”分隔的字符串。

binding key中可以存在两种特殊字符“*” 与 “#”，用于做模糊匹配，其中“*”用于匹配一个单词，“#”用于匹配多个单词（可以是零个）。

![img](https://mmbiz.qpic.cn/mmbiz_png/NIibUFNmgiawqYrmSGu8F369cvOO125qRrticAvqVu0q6HohaLeX3ofVsdaoeoLNsqEFdmVyzLY6jG8dhGrYCiaiaUg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



以上图中的配置为例，routingKey=”quick.orange.rabbit”的消息会同时路由到Q1与Q2，routingKey=”lazy.orange.fox”的消息会路由到Q1与Q2，routingKey=”lazy.brown.fox”的消息会路由到Q2，routingKey=”lazy.pink.rabbit”的消息会路由到Q2（只会投递给Q2一次，虽然这个routingKey与Q2的两个bindingKey都匹配）；routingKey=”quick.brown.fox”、routingKey=”orange”、routingKey=”quick.orange.male.rabbit”的消息将会被丢弃，因为它们没有匹配任何bindingKey。

**headers**

headers类型的Exchange不依赖于routing key与binding key的匹配规则来路由消息，而是根据发送的消息内容中的headers属性进行匹配。

在绑定Queue与Exchange时指定一组键值对；当消息发送到Exchange时，RabbitMQ会取到该消息的headers（也是一个键值对的形式），对比其中的键值对是否完全匹配Queue与Exchange绑定时指定的键值对；如果完全匹配则消息会路由到该Queue，否则不会路由到该Queue。

该类型的Exchange没有用到过（不过也应该很有用武之地），所以不做介绍。

## **11、RPC**

MQ本身是基于异步的消息处理，前面的示例中所有的生产者（P）将消息发送到RabbitMQ后不会知道消费者（C）处理成功或者失败（甚至连有没有消费者来处理这条消息都不知道）。

但实际的应用场景中，我们很可能需要一些同步处理，需要同步等待服务端将我的消息处理完成后再进行下一步处理。这相当于RPC（Remote Procedure Call，远程过程调用）。在RabbitMQ中也支持RPC。

![img](https://mmbiz.qpic.cn/mmbiz_png/NIibUFNmgiawqYrmSGu8F369cvOO125qRrOibMlLhcJl6CxtD5pribW1hP293BLE0cEIS2lFp4Khib7y8TaDyYHaGNg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



RabbitMQ实现RPC的机制是：

- 客户端发送请求（消息）时，在消息的属性（MessageProperties，在AMQP协议中定义了14中properties，这些属性会随着消息一起发送）中设置两个值replyTo（一个Queue名称，用于告诉服务器处理完成后将通知我的消息发送到这个Queue中）和correlationId（此次请求的标识号，服务器处理完成后需要将此属性返还，客户端将根据这个id了解哪条请求被成功执行了或执行失败）；
- 服务器端收到消息并处理；
- 服务器端处理完消息后，将生成一条应答消息到replyTo指定的Queue，同时带上correlationId属性；
- 客户端之前已订阅replyTo指定的Queue，从中收到服务器的应答消息后，根据其中的correlationId属性分析哪条请求被执行了，根据执行结果进行后续业务处理。

参考：https://mp.weixin.qq.com/s/7fFkRuNNygQy2g-IP2KOdg



