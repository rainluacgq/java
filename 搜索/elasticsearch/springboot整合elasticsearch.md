### 使用SpringBoot整合ElasticSearch

####  下载与安装：

下载地址：

下载Elasticsearch 6.2.2的zip包，并解压到指定目录，下载地址：https://www.elastic.co/cn/downloads/past-releases/elasticsearch-7.8.0

启动后访问：http://localhost:9200

![image-20201108180003913](https://github.com/rainluacgq/java/blob/master/搜索/elasticsearch/pic/image-20201108180003913.png)

Kibana下载地址： https://artifacts.elastic.co/downloads/kibana/kibana-7.8.0-windows-x86_64.zip

启动并访问： http://localhost:5601

![image-20201108175913647](https://github.com/rainluacgq/java/blob/master/搜索/elasticsearch/pic/image-20201108175913647.png)

集群状态查看：

```
GET /_cat/health?v
```

```
epoch      timestamp cluster       status node.total node.data shards pri relo init unassign pending_tasks max_task_wait_time active_shards_percent
1604817985 06:46:25  elasticsearch green           1         1      6   6    0    0        0             7              24.2m                100.0%
```

查看节点状态：

```
GET /_cat/indices?v
```

```
health status index                          uuid                   pri rep docs.count docs.deleted store.size pri.store.size
green  open   .kibana-event-log-7.8.0-000001 CxjwUxM-Q1GrzuK2V_z1FA   1   0          1            0      5.3kb          5.3kb
green  open   .apm-custom-link               Nc92z4paS4q7bzSE5R7Gdw   1   0          0            0       208b           208b
green  open   .kibana_task_manager_1         7s9DuIN2SGG3Tco9fObbFA   1   0          5           15     50.2kb         50.2kb
green  open   .apm-agent-configuration       R4BC5MMmSI65s2bezK6GUA   1   0          0            0       208b           208b
yellow open   pms                            _0imv_vURdm06PZlLMsLBg   1   1          1            1      9.3kb          9.3kb
green  open   .kibana_1                      _dYmcGsuQy-0qdlM2k02ow   1   0         40            2     33.2kb         33.2kb
```

获取某个节点的信息：

```
GET /pms
```

```
{
  "pms" : {
    "aliases" : { },
    "mappings" : {
      "properties" : {
        "_class" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "brandId" : {
          "type" : "long"
        },
        "id" : {
          "type" : "long"
        }
      }
    },
    "settings" : {
      "index" : {
        "creation_date" : "1604821080352",
        "number_of_shards" : "1",
        "number_of_replicas" : "1",
        "uuid" : "_0imv_vURdm06PZlLMsLBg",
        "version" : {
          "created" : "7080099"
        },
        "provided_name" : "pms"
      }
    }
  }
}
```

查询某个节点的数据：

```json
POST /pms/_search
{
  "from": 0, 
  "size": 2, 
  "query": {
    "multi_match": {
      "query": "hello",
      "fields": [
        "name",
        "brandName"
      ]
    }
  }
}
```

```json
{
  "took" : 0,
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
    "max_score" : 0.2876821,
    "hits" : [
      {
        "_index" : "pms",
        "_type" : "_doc",
        "_id" : "1",
        "_score" : 0.2876821,
        "_source" : {
          "_class" : "com.nationalday.demo.es.EsProduct",
          "id" : 1,
          "brandId" : 1,
          "brandName" : "hello world ,little breast"
        }
      }
    ]
  }
}
```

查询单个字符：

```
GET /_search
{
  "query": {
    "wildcard": {"brandName": "h*"}
  }
}
```

wildcard不支持多个字段的查询

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