### Flink实战

下载地址：https://flink.apache.org/downloads.html

启动flink：执行start-cluster.sh

访问地址  http://localhost:8081/#/

![image-20200713155232885](https://github.com/rainluacgq/java/tree/master/大数据/pic/image-20200713155232885.png)

重要接口一览：

DataSet 和DataStream  

Flink具有特殊类`DataSet`并`DataStream`在程序中表示数据。您可以将它们视为可以包含重复项的不可变数据集合。在`DataSet`（面向批处理的接口）数据有限的情况下，对于一个`DataStream`（面向流式处理的接口）数据元的数量可以是无界的。

1.Environment  

执行环境 StreamExecutionEnvironment 是所有 Flink 程序的基础。  

```java
创建执行环境有三种方式，分别为：
StreamExecutionEnvironment.getExecutionEnvironment
StreamExecutionEnvironment.createLocalEnvironment
StreamExecutionEnvironment.createRemoteEnvironment  
```

2.Source  

基于 File 的数据源     readTextFile(path)   

基于 Socket 的数据源    socketTextStream  

基于集合（ Collection）的数据源   env.fromCollection(list)  

3.Sink

写入文件   writeAsText  / WriteAsCsv  

print()

writeToSocket   根据 SerializationSchema 将元素写入到 socket 中。  

4.Transformation   

Map  : DataStream → DataStream：输入一个参数产生一个参数  

FlatMap  : DataStream → DataStream：输入一个参数，产生 0 个、 1 个或者多个输出。  

Filter  : DataStream → DataStream：结算每个元素的布尔值，并返回布尔值为 true 的元素。   

Split  : DataStream → SplitStream：根据某些特征把一个 DataStream 拆分成两个或者多个 DataStream。  

KeyBy  : DataStream → KeyedStream：输入必须是 Tuple 类型，逻辑地将一个流拆分成不相交的分区，每个分区包含具有相同 key 的元素，在内部以 hash 的形式实现的。  

Reduce  :KeyedStream → DataStream：一个分组数据流的聚合操作，合并当前的元素和上次聚合的结果，产生一个新的值，返回的流中包含每一次聚合的结果，而不是只返回最后一次聚合的最终结果。  ****

### 实战：基于Socket 统计word count

##### 一、引入依赖

```xml
<properties>
    <flink.version>1.10.0</flink.version>
    <scala.binary.version>2.11</scala.binary.version>
</properties>
<dependency>
    <groupId>org.apache.flink</groupId>
    <artifactId>flink-streaming-java_${scala.binary.version}</artifactId>
    <version>${flink.version}</version>
    <scope>provided</scope>
</dependency>
```

二、启动socket 测试

```bash
nc -lk 9999
```

三、测试代码

Flink程序包含以下流程：

 获得一个执行环境； （ Execution Environment）
 加载/创建初始数据； （ Source）
 指定转换这些数据； （ Transformation）
 指定放置计算结果的位置； （ Sink）
 触发程序执行。  

```java
public class WordCount {
    public static void main(String[] args) throws Exception {
        // final MultipleParameterTool params = MultipleParameterTool.fromArgs(args);
        // 设置执行环境 非stream
        // final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //设置数据源
        DataStreamSource<String> stream = env.socketTextStream("host_ip", 9999);
        //计数
        SingleOutputStreamOperator<Tuple2<String, Integer>> sum = stream.flatMap(new LineSplitter())
                .keyBy(0)
                .sum(1);
		//打印结果
        sum.print();
		//执行任务
        env.execute("Java WordCount from SocketTextStream Example");
    }
        public static final class LineSplitter implements FlatMapFunction<String, Tuple2<String, Integer>> {
            @Override
            public void flatMap(String s, Collector<Tuple2<String, Integer>> collector) {
                String[] tokens = s.toLowerCase().split("\\W+");

                for (String token: tokens) {
                    if (token.length() > 0) {
                        collector.collect(new Tuple2<String, Integer>(token, 1));
                    }
                }
            }
        }
}
```

四、测试结果

在服务端输入以下代码：

![image-20200715135900785](https://github.com/rainluacgq/java/tree/master/大数据/pic/image-20200715135900785.png)

窗口打印：

![image-20200715140115805](https://github.com/rainluacgq/java/tree/master/大数据/pic/image-20200715140115805.png)



