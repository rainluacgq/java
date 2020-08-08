### Flink集成ELK大屏展示

1.添加依赖

```xml
<dependency>
    <groupId>org.apache.flink</groupId>
    <artifactId>flink-streaming-java_${scala.binary.version}</artifactId>
    <version>${flink.version}</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>org.apache.flink</groupId>
    <artifactId>flink-connector-kafka-0.11_2.11</artifactId>
    <version>1.10.0</version>
</dependency>
```

查看kafka生成的数据：

```shell
kafka-console-consumer.sh --topic hello  --bootstrap-server localhost:9092 --from-beginning --max-messages 10  
```

下载依赖的jar包并复制到lib文件夹下：

```shell
wget -P ./lib/ https://repo1.maven.org/maven2/org/apache/flink/flinkjson/1.10.0/flink-json-1.10.0.jar | \
wget -P ./lib/ https://repo1.maven.org/maven2/org/apache/flink/flinksql-connector-kafka_2.11/1.10.0/flink-sql-connectorkafka_2.11-1.10.0.jar | \
wget -P ./lib/ https://repo1.maven.org/maven2/org/apache/flink/flinksql-connector-elasticsearch6_2.11/1.10.0/flink-sqlconnector-elasticsearch6_2.11-1.10.0.jar | \
wget -P ./lib/ https://repo1.maven.org/maven2/org/apache/flink/flinkjdbc_2.11/1.10.0/flink-jdbc_2.11-1.10.0.jar | \
wget -P ./lib/ https://repo1.maven.org/maven2/mysql/mysql-connectorjava/5.1.48/mysql-connector-java-5.1.48.jar  
```

创建DDl连接kafka的数据源

```sql

CREATE TABLE test_result (
orderNum STRING,
semiSn STRING,
finsSn STRING,
station STRING,
errCode TINYINT,
ts TIMESTAMP(3),
proctime as PROCTIME(), -- 通过计算列产生一个处理时间列
WATERMARK FOR ts as ts - INTERVAL '5' SECOND -- 在 ts 上定义 watermark， ts 成为事件时间列
) WITH (
'connector.type' = 'kafka', -- 使用 kafka connector
'connector.version' = 'universal', -- kafka 版本， universal 支持 0.11 以上的版本
'connector.topic' = 'test_result', -- kafka topic
'connector.startup-mode' = 'earliest-offset', -- 从起始 offset 开始读取
'connector.properties.zookeeper.connect' = 'localhost:2181', --zookeeper 地址
'connector.properties.bootstrap.servers' = 'localhost:9092', -- kafkabroker 地址
'format.type' = 'json'  -- 数据源格式为 json
);  
```

创建ES结果表

```sql
CREATE TABLE order_cnt_per_day (
day_of_month BIGINT,
order_cnt BIGINT
) WITH (
'connector.type' = 'elasticsearch', -- 使用 elasticsearch connector
'connector.version' = '6', -- elasticsearch 版本， 6 能支持 es 6+ 以及 7+ 版本
'connector.hosts' = 'http://localhost:9200', -- elasticsearch 地址
'connector.index' = 'order_cnt_per_day', -- elasticsearch 索引名，相当于数据库的表名
'connector.document-type' = 'test_result', -- elasticsearch 的 type，相当于数据库的库名
'connector.bulk-flush.max-actions' = '1', -- 每条数据都刷新
'format.type' = 'json', -- 输出数据格式 json
'update-mode' = 'append'
);  
```

参考：https://ci.apache.org/projects/flink/flink-docs-release-1.10/zh/dev/table/connect.html#elasticsearch-connector

其中 update-mode支持 *append*  和 *upsert* 两种模式，append模式仅支持追加记录，如果想做更新操作，可使用uosert。

提交query

```sql
INSERT INTO order_cnt_per_day
SELECT HOUR(TUMBLE_START(ts, INTERVAL '1' HOUR)), COUNT(*)
FROM test_result
GROUP BY TUMBLE(ts, INTERVAL '1' HOUR);  
```

配置Kibana

![image-20200804155426548](C:\Users\caiguoqing\AppData\Roaming\Typora\typora-user-images\image-20200804155426548.png)

保存之后即可看到：

![image-20200804155514493](C:\Users\caiguoqing\AppData\Roaming\Typora\typora-user-images\image-20200804155514493.png)