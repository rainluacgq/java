###  SpringCloud整合Seata实现分布式事务管理

### 一、概念一览

## [什么是分布式事务问题？](https://macrozheng.github.io/mall-learning/#/cloud/seata?id=什么是分布式事务问题？)

### [单体应用](https://macrozheng.github.io/mall-learning/#/cloud/seata?id=单体应用)

单体应用中，一个业务操作需要调用三个模块完成，此时数据的一致性由本地事务来保证。

![img](https://macrozheng.github.io/mall-learning/images/springcloud_seata_05.png)

### [微服务应用](https://macrozheng.github.io/mall-learning/#/cloud/seata?id=微服务应用)

随着业务需求的变化，单体应用被拆分成微服务应用，原来的三个模块被拆分成三个独立的应用，分别使用独立的数据源，业务操作需要调用三个服务来完成。此时每个服务内部的数据一致性由本地事务来保证，但是全局的数据一致性问题没法保证。

![img](https://macrozheng.github.io/mall-learning/images/springcloud_seata_06.png)

### [小结](https://macrozheng.github.io/mall-learning/#/cloud/seata?id=小结)

在微服务架构中由于全局数据一致性没法保证产生的问题就是分布式事务问题。简单来说，一次业务操作需要操作多个数据源或需要进行远程调用，就会产生分布式事务问题。

各事务模式参考：https://seata.io/zh-cn/docs/dev/mode/at-mode.html

## [Seata简介](https://macrozheng.github.io/mall-learning/#/cloud/seata?id=seata简介)

Seata 是一款开源的分布式事务解决方案，致力于提供高性能和简单易用的分布式事务服务。Seata 为用户提供了 AT、TCC、SAGA 和 XA 事务模式，为用户打造一站式的分布式解决方案。

## [Seata原理和设计](https://macrozheng.github.io/mall-learning/#/cloud/seata?id=seata原理和设计)

### [定义一个分布式事务](https://macrozheng.github.io/mall-learning/#/cloud/seata?id=定义一个分布式事务)

我们可以把一个分布式事务理解成一个包含了若干分支事务的全局事务，全局事务的职责是协调其下管辖的分支事务达成一致，要么一起成功提交，要么一起失败回滚。此外，通常分支事务本身就是一个满足ACID的本地事务。这是我们对分布式事务结构的基本认识，与 XA 是一致的。

![img](https://macrozheng.github.io/mall-learning/images/springcloud_seata_07.png)

### [协议分布式事务处理过程的三个组件](https://macrozheng.github.io/mall-learning/#/cloud/seata?id=协议分布式事务处理过程的三个组件)

- Transaction Coordinator (TC)： 事务协调器，维护全局事务的运行状态，负责协调并驱动全局事务的提交或回滚；
- Transaction Manager (TM)： 控制全局事务的边界，负责开启一个全局事务，并最终发起全局提交或全局回滚的决议；
- Resource Manager (RM)： 控制分支事务，负责分支注册、状态汇报，并接收事务协调器的指令，驱动分支（本地）事务的提交和回滚。

![img](https://macrozheng.github.io/mall-learning/images/springcloud_seata_08.png)

原理概览：

![image-20200718161057016](C:\Users\19349\AppData\Roaming\Typora\typora-user-images\image-20200718161057016.png)

### [一个典型的分布式事务过程](https://macrozheng.github.io/mall-learning/#/cloud/seata?id=一个典型的分布式事务过程)

- TM 向 TC 申请开启一个全局事务，全局事务创建成功并生成一个全局唯一的 XID；
- XID 在微服务调用链路的上下文中传播；
- RM 向 TC 注册分支事务，将其纳入 XID 对应全局事务的管辖；
- TM 向 TC 发起针对 XID 的全局提交或回滚决议；
- TC 调度 XID 下管辖的全部分支事务完成提交或回滚请求。

![img](https://macrozheng.github.io/mall-learning/images/springcloud_seata_09.png)

### 二、环境配置

下载安装seata-server，地址是https://github.com/seata/seata/releases

使用Eureka等作为注册中心

修改conf目录下的file.conf文件

```bash
service {
  #vgroup->rgroup
  vgroup_mapping.fsp_tx_group = "default" #修改事务组名称为：fsp_tx_group，和客户端自定义的名称对应
  #only support single node
  default.grouplist = "127.0.0.1:8091"
  #degrade current not support
  enableDegrade = false
  #disable
  disable = false
  #unit ms,s,m,h,d represents milliseconds, seconds, minutes, hours, days, default permanent
  max.commit.retry.timeout = "-1"
  max.rollback.retry.timeout = "-1"
}

## transaction log store
store {
  ## store mode: file、db
  mode = "db" #修改此处将事务信息存储到数据库中

  ## database store
  db {
    ## the implement of javax.sql.DataSource, such as DruidDataSource(druid)/BasicDataSource(dbcp) etc.
    datasource = "dbcp"
    ## mysql/oracle/h2/oceanbase etc.
    db-type = "mysql"
    driver-class-name = "com.mysql.jdbc.Driver"
    url = "jdbc:mysql://localhost:3306/seat-server" #修改数据库连接地址
    user = "root" #修改数据库用户名
    password = "root" #修改数据库密码
    min-conn = 1
    max-conn = 3
    global.table = "global_table"
    branch.table = "branch_table"
    lock-table = "lock_table"
    query-limit = 100
  }
}
```

- 修改`conf`目录下的`registry.conf`配置文件，指明注册中心为`nacos`，及修改`nacos`连接信息即可；

```bash
registry {
  # file 、nacos 、eureka、redis、zk、consul、etcd3、sofa
  type = "eureka" #改为nacos

  nacos {
    serverAddr = "localhost:8848" #改为nacos的连接地址
    namespace = ""
    cluster = "default"
  }
  eureka {
      serviceUrl = "http://localhost:8001/eureka" ## 修改成eureka的注册中心地址
      application = "default"
      weight = "1"
  }
}
```

- 先启动Eureka/Nacos，再使用seata-server中`/bin/seata-server.bat`文件启动seata-server。

####  三、集成springcloud

查询mvn依赖https://mvnrepository.com/artifact/io.seata/seata-all，依赖：

```xml
<!--seata-->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
    <exclusions>
        <exclusion>
            <artifactId>seata-all</artifactId>
            <groupId>io.seata</groupId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>io.seata</groupId>
    <artifactId>seata-all</artifactId>
    <version>${seata.version}</version>
</dependency>
```

配置：

```yaml
spring:
  application:
    name: seata-order-service
  cloud:
    alibaba:
      seata:
        txservice--group: fsp_tx_group		###需要跟config文件中一致
```

一、集成springcloud的代码

1.移除DataSourceAutoConfiguration

```java
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
```

2.配置代理为seata动态代理

```java
package com.macro.cloud.config;

import com.alibaba.druid.pool.DruidDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * 使用Seata对数据源进行代理
 * Created by macro on 2019/11/11.
 */
@Configuration
public class DataSourceProxyConfig {

    @Value("${mybatis.mapperLocations}")
    private String mapperLocations;

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource druidDataSource(){
        return new DruidDataSource();
    }

    @Bean
    public DataSourceProxy dataSourceProxy(DataSource dataSource) {
        return new DataSourceProxy(dataSource);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactoryBean(DataSourceProxy dataSourceProxy) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSourceProxy);
        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources(mapperLocations));
        sqlSessionFactoryBean.setTransactionFactory(new SpringManagedTransactionFactory());
        return sqlSessionFactoryBean.getObject();
    }

}
```

在需要事务控制的方法上增加

```java
 @GlobalTransactional(name = "fsp-create-order",rollbackFor = Exception.class)
 public void create(Order order) {
     /**业务代码**/
 }
```

#### 附录

file.conf 配置文件 

如果使用file config配置，则需要在在每个服务中都增加如下配置：

```bash
transport {
  # tcp udt unix-domain-socket
  type = "TCP"
  #NIO NATIVE
  server = "NIO"
  #enable heartbeat
  heartbeat = true
  #thread factory for netty
  thread-factory {
    boss-thread-prefix = "NettyBoss"
    worker-thread-prefix = "NettyServerNIOWorker"
    server-executor-thread-prefix = "NettyServerBizHandler"
    share-boss-worker = false
    client-selector-thread-prefix = "NettyClientSelector"
    client-selector-thread-size = 1
    client-worker-thread-prefix = "NettyClientWorkerThread"
    # netty boss thread size,will not be used for UDT
    boss-thread-size = 1
    #auto default pin or 8
    worker-thread-size = 8
  }
  shutdown {
    # when destroy server, wait seconds
    wait = 3
  }
  serialization = "seata"
  compressor = "none"
}

service {
  #vgroup->rgroup
  vgroup_mapping.fsp_tx_group = "default"
  #only support single node
  default.grouplist = "127.0.0.1:8091"
  #degrade current not support
  enableDegrade = false
  #disable
  disable = false
  #unit ms,s,m,h,d represents milliseconds, seconds, minutes, hours, days, default permanent
  max.commit.retry.timeout = "-1"
  max.rollback.retry.timeout = "-1"
  disableGlobalTransaction = false
}

client {
  async.commit.buffer.limit = 10000
  lock {
    retry.internal = 10
    retry.times = 30
  }
  report.retry.count = 5
  tm.commit.retry.count = 1
  tm.rollback.retry.count = 1
}

transaction {
  undo.data.validation = true
  undo.log.serialization = "jackson"
  undo.log.save.days = 7
  #schedule delete expired undo_log in milliseconds
  undo.log.delete.period = 86400000
  undo.log.table = "undo_log"
}

support {
  ## spring
  spring {
    # auto proxy the DataSource bean
    datasource.autoproxy = false
  }
}
```

undo log，seats默认使用AT模式，需要创建该表

## 回滚日志表

UNDO_LOG Table：不同数据库在类型上会略有差别。

以 MySQL 为例：

| Field         | Type         |
| ------------- | ------------ |
| branch_id     | bigint PK    |
| xid           | varchar(100) |
| context       | varchar(128) |
| rollback_info | longblob     |
| log_status    | tinyint      |
| log_created   | datetime     |
| log_modified  | datetime     |

```sql

```

```sql
CREATE TABLE IF NOT EXISTS  `undo_log`
(
    `id`            BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT 'increment id',
    `branch_id`     BIGINT(20)   NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(100) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT(11)      NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME     NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME     NOT NULL COMMENT 'modify datetime',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8 COMMENT ='AT transaction mode undo table';
```

参考：

https://macrozheng.github.io/mall-learning/#/cloud/seata