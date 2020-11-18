### 使用SpringBoot整合ElasticSearch

####  下载与安装：

下载地址：

下载Elasticsearch 6.2.2的zip包，并解压到指定目录，下载地址：https://www.elastic.co/cn/downloads/past-releases/elasticsearch-7.8.0

启动后访问：http://localhost:9200

![image-20201108180003913](https://github.com/rainluacgq/java/blob/master/搜索/elasticsearch/pic/image-20201108180003913.png)

Kibana下载地址： https://artifacts.elastic.co/downloads/kibana/kibana-7.8.0-windows-x86_64.zip

启动并访问： http://localhost:5601

![image-20201108175913647](https://github.com/rainluacgq/java/blob/master/搜索/elasticsearch/pic/image-20201108175913647.png)

###  整合springboot：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

修改配置：

```yml
spring:
  data:
    elasticsearch:
      repositories:
        enabled: true
      cluster-nodes: 127.0.0.1:9300
```

注意修改客户端版本，默认是7.6.2 修改成7.8.0：

![image-20201108180216232](https://github.com/rainluacgq/java/blob/master/搜索/elasticsearch/pic/image-20201108180216232.png)

1. 实现接口

```java
public interface EsProductRepository extends ElasticsearchRepository<EsProduct, Long> {

    Page<EsProduct> findByBrandName(String keywords,Pageable page);
}
```

2.常用接口：

```java
//  1.插入/更新数据
esProductRepository.save(esProduct); //保存数据
// 2.分页查询数据
Pageable pageable = PageRequest.of(0, 10);
Page<EsProduct> esProductPage  =  esProductRepository.findByBrandName(keyword,pageable);
// 3. 复杂查询
ElasticsearchRestTemplate elasticsearchRestTemplate;
```

3.实例

```java
@Document(indexName = "pms", type = "product",shards = 1,replicas = 0)
@Getter
@Setter
public class EsProduct implements Serializable {
    private static final long serialVersionUID = -1L;
    @Id
    private Long id;
    @Field(type = FieldType.Keyword)
    private String productSn;
    private Long brandId;
    @Field(type = FieldType.Keyword)
}
```

注： type后续即将被废弃

常用注解一览：

```java
public @interface Document {
 //索引库名次，mysql中数据库的概念
   String indexName();

   /**
    * Mapping type name. <br/>
    * deprecated as Elasticsearch does not support this anymore
    * (@see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/7.3/removal-of-types.html">Elastisearch removal of types documentation</a>) and will remove it in
    * Elasticsearch 8.
    *
    * @deprecated since 4.0
    */
   @Deprecated
   String type() default "";

   /**
    * Use server-side settings when creating the index.
    */
   boolean useServerConfiguration() default false;

   /**
    * Number of shards for the index {@link #indexName()}. Used for index creation. <br/>
    * With version 4.0, the default value is changed from 5 to 1 to reflect the change in the default settings of
    * Elasticsearch which changed to 1 as well in Elasticsearch 7.0.
    */
   short shards() default 1;

   /**
    * Number of replicas for the index {@link #indexName()}. Used for index creation.
    */
   short replicas() default 1;

   /**
    * Refresh interval for the index {@link #indexName()}. Used for index creation.
    */
   String refreshInterval() default "1s";

   /**
    * Index storage type for the index {@link #indexName()}. Used for index creation.
    */
   String indexStoreType() default "fs";

   /**
    * Configuration whether to create an index on repository bootstrapping.
    */
   boolean createIndex() default true;

   /**
    * Configuration of version management.
    */
   VersionType versionType() default VersionType.EXTERNAL;
```

字段含义：

```java
public @interface Field {

   /**
    * Alias for {@link #name}.
    *
    * @since 3.2
    */
   @AliasFor("name")
   String value() default "";

   /**
    * The <em>name</em> to be used to store the field inside the document.
    * <p>
    * √5 If not set, the name of the annotated property is used.
    *
    * @since 3.2
    */
   @AliasFor("value")
   String name() default "";

   FieldType type() default FieldType.Auto;

   boolean index() default true;   //是否建立倒排索引

   DateFormat format() default DateFormat.none;

   String pattern() default "";

   boolean store() default false;

   boolean fielddata() default false;

   String searchAnalyzer() default "";

   String analyzer() default "";

   String normalizer() default "";

   String[] ignoreFields() default {};

   boolean includeInParent() default false;

   String[] copyTo() default {};

   /**
    * @since 4.0
    */
   int ignoreAbove() default -1;

   /**
    * @since 4.0
    */
   boolean coerce() default true;

   /**
    * @since 4.0
    */
   boolean docValues() default true;

   /**
    * @since 4.0
    */
   boolean ignoreMalformed() default false;

   /**
    * @since 4.0
    */
   IndexOptions indexOptions() default IndexOptions.none;

   /**
    * @since 4.0
    */
   boolean indexPhrases() default false;

   /**
    * implemented as array to enable the empty default value
    *
    * @since 4.0
    */
   IndexPrefixes[] indexPrefixes() default {};

   /**
    * @since 4.0
    */
   boolean norms() default true;

   /**
    * @since 4.0
    */
   String nullValue() default "";

   /**
    * @since 4.0
    */
   int positionIncrementGap() default -1;

   /**
    * @since 4.0
    */
   Similarity similarity() default Similarity.Default;

   /**
    * @since 4.0
    */
   TermVector termVector() default TermVector.none;

   /**
    * @since 4.0
    */
   double scalingFactor() default 1;

   /**
    * @since 4.0
    */
   int maxShingleSize() default -1;
}
```

```java
public enum FieldType {
    Auto, //
    Text, //会进行分词并建了索引的字符类型
    Keyword, // 不会进行分词建立索引的类型
    Long, //
    Integer, //
    Short, //
    Byte, //
    Double, //
    Float, //
    Half_Float, //
    Scaled_Float, //
    Date, //
    Date_Nanos, //
    Boolean, //
    Binary, //
    Integer_Range, //
    Float_Range, //
    Long_Range, //
    Double_Range, //
    Date_Range, //
    Ip_Range, //
    Object, //
    Nested, // 嵌套类型
    Ip, //
    TokenCount, //
    Percolator, //
    Flattened, //
    Search_As_You_Type //
}
```

### 中文支持

中文分词器下载：https://github.com/medcl/elasticsearch-analysis-ik

下载完成后，直接解压放到es的plugun文件夹下，在需要中文的字段下：

```java
@Field(analyzer = "ik_max_word",type = FieldType.Text)
private String productCategoryName;
```

测试：

```
POST /pms/_search
{
  "from": 0, 
  "size": 2, 
  "query": {
    "multi_match": {
      "query": "川&普",
      "fields": [
        "name",
        "brandName",
        "productCategoryName"
      ]
    }
  }
}
```

```
{
  "took" : 1,
  "timed_out" : false,
  "_shards" : {
    "total" : 1,
    "successful" : 1,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : {
      "value" : 1,
      "relation" : "eq"
    },
    "max_score" : 0.5753642,
    "hits" : [
      {
        "_index" : "pms",
        "_type" : "_doc",
        "_id" : "1",
        "_score" : 0.5753642,
        "_source" : {
          "_class" : "com.nationalday.demo.es.EsProduct",
          "id" : 1,
          "brandId" : 1,
          "brandName" : "hello world ,little breast",
          "productCategoryName" : "yellow month month bird 川普和拜登谁将赢得大选"
        }
      }
    ]
  }
}
```

```
POST /pms/_search
{
  "from": 0, 
  "size": 2, 
  "query": {
    "multi_match": {
      "query": "四川",
      "fields": [
        "name",
        "brandName",
        "productCategoryName"
      ]
    }
  }
}

```

使用四川也可搜索

参考： https://www.elastic.co/guide/en/elasticsearch/reference/7.x/index.html