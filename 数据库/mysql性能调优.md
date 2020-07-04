### mysql性能调优

#### mysql性能优化

- MySQL性能

- - 最大数据量

- 《阿里巴巴Java开发手册》提出单表行数超过500万行或者单表容量超过2GB，才推荐分库分表。性能由综合因素决定，抛开业务复杂度，影响程度依次是硬件配置、MySQL配置、数据表设计、索引优化。500万这个值仅供参考，并非铁律。

- - 最大并发数

    并发数是指同一时刻数据库能处理多少个请求，由max_connections和max_user_connections决定。max_connections是指MySQL实例的最大连接数，上限值是16384，max_user_connections是指每个数据库用户的最大连接数。

    MySQL会为每个连接提供缓冲区，意味着消耗更多的内存。如果连接数设置太高硬件吃不消，太低又不能充分利用硬件。一般要求两者比值超过10%，计算方法如下：

    ```mysql
    max_used_connections / max_connections * 100% = 3/100 *100% ≈ 3%
    ```

  - 查询耗时0.5秒

- 建议将单次查询耗时控制在0.5秒以内，0.5秒是个经验值，源于用户体验的3秒原则。如果用户的操作3秒内没有响应，将会厌烦甚至退出。响应时间=客户端UI渲染耗时+网络请求耗时+应用程序处理耗时+查询数据库耗时，0.5秒就是留给数据库1/6的处理时间。

- - 实施原则
    - 充分利用但不滥用索引，须知索引也消耗磁盘和CPU。
    - 不推荐使用数据库函数格式化数据，交给应用程序处理。
    - 不推荐使用外键约束，用应用程序保证数据准确性。
    - 写多读少的场景，不推荐使用唯一索引，用应用程序保证唯一性。
    - 适当冗余字段，尝试创建中间表，用应用程序计算中间结果，用空间换时间。
    - 不允许执行极度耗时的事务，配合应用程序拆分成更小的事务。
    - 预估重要数据表（比如订单表）的负载和数据增长态势，提前优化。

- 数据表设计

- - 数据类型

- 数据类型的选择原则：更简单或者占用空间更小。

- 如果长度能够满足，整型尽量使用tinyint、smallint、medium_int而非int。

- 如果字符串长度确定，采用char类型。

- 如果varchar能够满足，不采用text类型。

- 精度要求较高的使用decimal类型，也可以使用BIGINT，比如精确两位小数就乘以100后保存。

- 尽量采用timestamp而非datetime。

- ![img](https://mmbiz.qpic.cn/mmbiz_png/JfTPiahTHJhoSNMK1rQe1nMzP7wHQpYxZpBpcFjJSa7Jskae0kzic1I7hO5MWC265XUb1hKh1H9bFxvq6JU9ho2g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

- 相比datetime，timestamp占用更少的空间，以UTC的格式储存自动转换时区。

- - 避免空值

    MySQL中字段为NULL时依然占用空间，会使索引、索引统计更加复杂。从NULL值更新到非NULL无法做到原地更新，容易发生索引分裂影响性能。尽可能将NULL值用有意义的值代替，也能避免SQL语句里面包含is not null的判断

  - text类型

- 由于text字段储存大量数据，表容量会很早涨上去，影响其他字段的查询性能。建议抽取出来放在子表里，用业务主键关联。

- 索引优化

- - 索引分类

- 普通索引：最基本的索引。

- 组合索引：多个字段上建立的索引，能够加速复合查询条件的检索。

- 唯一索引：与普通索引类似，但索引列的值必须唯一，允许有空值。

- 组合唯一索引：列值的组合必须唯一。

- 主键索引：特殊的唯一索引，用于唯一标识数据表中的某一条记录，不允许有空值，一般用primary key约束。

- 全文索引：用于海量文本的查询，MySQL5.6之后的InnoDB和MyISAM均支持全文索引。由于查询精度以及扩展性不佳，更多的企业选择Elasticsearch。

- - 优化原则

- 分页查询很重要，如果查询数据量超过30%，MYSQL不会使用索引。

- 单表索引数不超过5个、单个索引字段数不超过5个。

- 字符串可使用前缀索引，前缀长度控制在5-8个字符。

- 字段唯一性太低，增加索引没有意义，如：是否删除、性别。

- 合理使用覆盖索引，如下所示：

- ```sql
  select login_name, nick_name from member where login_name = ?
  ```

- login_name, nick_name两个字段建立组合索引，比login_name简单索引要更快。

- SQL优化

- - 分批处理
  - 不做列运算
  - 避免Select *
  - 操作符<>优化
  - OR优化
  - IN优化
  - LIKE优化
  - JOIN优化
  - LIMIT优化

#### 配置优化

**数据库参数优化**

**①调整**

实例整体（高级优化，扩展）：

```ini
thread_concurrency：# 并发线程数量个数
sort_buffer_size：# 排序缓存
read_buffer_size：# 顺序读取缓存
read_rnd_buffer_size：# 随机读取缓存
key_buffer_size：# 索引缓存
thread_cache_size：# (1G—>8, 2G—>16, 3G—>32, >3G—>64)
```

**②连接层（基础优化）**

设置合理的连接客户和连接方式：

```ini
max_connections           # 最大连接数，看交易笔数设置    
max_connect_errors        # 最大错误连接数，能大则大
connect_timeout           # 连接超时
max_user_connections      # 最大用户连接数
skip-name-resolve         # 跳过域名解析
wait_timeout              # 等待超时
back_log                  # 可以在堆栈中的连接数量
```



**③SQL 层（基础优化）**

**query_cache_size：** 查询缓存 >>> OLAP 类型数据库，需要重点加大此内存缓存，但是一般不会超过 GB。

对于经常被修改的数据，缓存会马上失效。我们可以使用内存数据库（redis、memecache），替代它的功能。

**存储引擎层优化**

innodb 基础优化参数：

```ini
default-storage-engine
innodb_buffer_pool_size       # 没有固定大小，50%测试值，看看情况再微调。但是尽量设置不要超过物理内存70%
innodb_file_per_table=(1,0)
innodb_flush_log_at_trx_commit=(0,1,2) # 1是最安全的，0是性能最高，2折中
binlog_sync
Innodb_flush_method=(O_DIRECT, fdatasync)
innodb_log_buffer_size        # 100M以下
innodb_log_file_size          # 100M 以下
innodb_log_files_in_group     # 5个成员以下,一般2-3个够用（iblogfile0-N）
innodb_max_dirty_pages_pct   # 达到百分之75的时候刷写 内存脏页到磁盘。
log_bin
max_binlog_cache_size         # 可以不设置
max_binlog_size               # 可以不设置
innodb_additional_mem_pool_size    #小于2G内存的机器，推荐值是20M。32G内存以上100M
```

参考：https://mp.weixin.qq.com/s/ZBgv-s-_ojWMiTZYTVx-iQ



