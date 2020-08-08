### Spring整合ElasticSearch实现数据统计

概念一览

Elasticsearch是一个基于Lucene的搜索服务器。它提供了一个分布式的全文搜索引擎，基于restful web接口。Elasticsearch是用Java语言开发的，基于Apache协议的开源项目，是目前最受欢迎的企业搜索引擎。Elasticsearch广泛运用于云计算中，能够达到实时搜索，具有稳定，可靠，快速的特点。

引入依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch<artifactId>
</dependency>
```

修改配置

```yml
data:
  elasticsearch:
    repositories:
      enabled: true
    cluster-nodes: 127.0.0.1:9300 # es的连接地址及端口号
    cluster-name: elasticsearch # es集群的名称
```

常用注解

```java
1. @Document
//标示映射到Elasticsearch文档上的领域对象
public @interface Document {
  //索引库名次，mysql中数据库的概念
    String indexName();
  //文档类型，mysql中表的概念
    String type() default "";
  //默认分片数
    short shards() default 5;
  //默认副本数量
    short replicas() default 1;
}

2. @Field
public @interface Field {
  //文档中字段的类型
    FieldType type() default FieldType.Auto;
  //是否建立倒排索引
    boolean index() default true;
  //是否进行存储
    boolean store() default false;
  //分词器名次
    String analyzer() default "";
}


//为文档自动指定元数据类型
public enum FieldType {
    Text,//会进行分词并建了索引的字符类型
    Integer,
    Long,
    Date,
    Float,
    Double,
    Boolean,
    Object,
    Auto,//自动判断字段类型
    Nested,//嵌套对象类型
    Ip,
    Attachment,
    Keyword//不会进行分词建立索引的类型
}
```







常用接口总结

