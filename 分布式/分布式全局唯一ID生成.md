### 分布式ID的几种生成方案

#### 1、UUID

这个方案是小伙伴们第一个能过考虑到的方案

**优点：**

- 代码实现简单。
- 本机生成，没有性能问题
- 因为是全球唯一的ID，所以迁移数据容易

**缺点：**

- 每次生成的ID是无序的，无法保证趋势递增
- UUID的字符串存储，查询效率慢
- 存储空间大
- ID本事无业务含义，不可读

**应用场景：**

- 类似生成token令牌的场景
- 不适用一些要求有趋势递增的ID场景

此UUID方案是不适用老顾的需求。

#### 2、MySQL主键自增

这个方案就是利用了MySQL的主键自增auto_increment，默认每次ID加1。

**优点：**

- 数字化，id递增
- 查询效率高
- 具有一定的业务可读

**缺点：**

- 存在单点问题，如果mysql挂了，就没法生成iD了
- 数据库压力大，高并发抗不住

#### 3、MySQL多实例主键自增

这个方案就是解决mysql的单点问题，在auto_increment基本上面，设置step步长

![img](https://mmbiz.qpic.cn/mmbiz_jpg/JdLkEI9sZffBdQ2u7mibDAK9aMrPQX92pmr0NBHA4e3UichBL8ny5Enzj2rTlfxAsk0pHMhzohYy6rtsrVduY4Jw/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

每台的初始值分别为1,2,3...N，步长为N（这个案例步长为4）

**优点：**

- 解决了单点问题

**缺点：**

- 一旦把步长定好后，就无法扩容；而且单个数据库的压力大，数据库自身性能无法满足高并发

**应用场景：**

- 数据不需要扩容的场景

此方案也不满足老顾的需求，因为不方便扩容（记住这个方案，嘿嘿）

#### 4、雪花snowflake算法

雪花算法生成64位的二进制正整数，然后转换成10进制的数。64位二进制数由如下部分组成：

![img](https://mmbiz.qpic.cn/mmbiz_jpg/JdLkEI9sZffBdQ2u7mibDAK9aMrPQX92p4Xs5vcxGoHURq3SiaU7ekNxqlMicicQNfHJSRFr6RYNzNBzMibkFjhJZuQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

- 1位标识符：始终是0
- 41位时间戳：41位时间截不是存储当前时间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截 )得到的值，这里的的开始时间截，一般是我们的id生成器开始使用的时间，由我们程序来指定的
- 10位机器标识码：可以部署在1024个节点，如果机器分机房（IDC）部署，这10位可以由 5位机房ID + 5位机器ID 组成
- 12位序列：毫秒内的计数，12位的计数顺序号支持每个节点每毫秒(同一机器，同一时间截)产生4096个ID序号

**优点：**

- 此方案每秒能够产生409.6万个ID，性能快
- 时间戳在高位，自增序列在低位，整个ID是趋势递增的，按照时间有序递增
- 灵活度高，可以根据业务需求，调整bit位的划分，满足不同的需求

**缺点：**

- 依赖机器的时钟，如果服务器时钟回拨，会导致重复ID生成

在分布式场景中，服务器时钟回拨会经常遇到，一般存在10ms之间的回拨；小伙伴们就说这点10ms，很短可以不考虑吧。但此算法就是建立在毫秒级别的生成方案，一旦回拨，就很有可能存在重复ID。

#### 5、Redis生成方案

利用redis的incr原子性操作自增，一般算法为：

年份 + 当天距当年第多少天 + 天数 + 小时 + redis自增

**优点：**

- 有序递增，可读性强

**缺点：**

- 占用带宽，每次要向redis进行请求

整体测试了这个性能如下：

```
127.0.0.1:6379> set seq_id 1     // 初始化自增ID为1
OK
127.0.0.1:6379> incr seq_id      // 增加1，并返回
(integer) 2
127.0.0.1:6379> incr seq_id      // 增加1，并返回
(integer) 3
需求：同时10万个请求获取ID1、并发执行完耗时：9s左右
2、单任务平均耗时：74ms
3、单线程最小耗时：不到1ms
4、单线程最大耗时：4.1s
```

参考：

https://mp.weixin.qq.com/s/7RQhCazoLJ-qO7CglZ6b2Q

[java Guide 分布式ID生成](https://github.com/Snailclimb/JavaGuide/blob/master/docs/system-design/micro-service/分布式id生成方案总结.md)