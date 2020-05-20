java基础

一、hashmap

![image-20200506205521386](https://github.com/rainluacgq/java/blob/master/java%E5%9F%BA%E7%A1%80/pic/image-20200506205521386.png)

 HashMap就是使用哈希表来存储的。哈希表为解决冲突，可以采用开放地址法和链地址法等来解决问题，Java中HashMap采用了链地址法。链地址法，简单来说，就是数组加链表的结合。在每个数组元素上都一个链表结构，当数据被Hash后，得到数组下标，把数据放在对应下标元素的链表上。例如程序执行下面代码：

```text
    map.put("美团","小美");
```

系统将调用"美团"这个key的hashCode()方法得到其hashCode 值（该方法适用于每个Java对象），然后再通过Hash算法的后两步运算（高位运算和取模运算，下文有介绍）来定位该键值对的存储位置，有时两个key会定位到相同的位置，表示发生了Hash碰撞。当然Hash算法计算结果越分散均匀，Hash碰撞的概率就越小，map的存取效率就会越高。

在理解Hash和扩容流程之前，我们得先了解下HashMap的几个字段。从HashMap的默认构造函数源码可知，构造函数就是对下面几个字段进行初始化，源码如下：

```text
     int threshold;             // 所能容纳的key-value对极限 
     final float loadFactor;    // 负载因子
     int modCount;  
     int size;
```

首先，Node[] table的初始化长度length(默认值是16)，Load factor为负载因子(默认值是0.75)，threshold是HashMap所能容纳的最大数据量的Node(键值对)个数。threshold = length * Load factor。也就是说，在数组定义好长度之后，负载因子越大，所能容纳的键值对个数越多。

结合负载因子的定义公式可知，threshold就是在此Load factor和length(数组长度)对应下允许的最大元素数目，超过这个数目就重新resize(扩容)，扩容后的HashMap容量是之前容量的两倍。默认的负载因子0.75是对空间和时间效率的一个平衡选择，建议大家不要修改，除非在时间和空间比较特殊的情况下，如果内存空间很多而又对时间效率要求很高，可以降低负载因子Load factor的值；相反，如果内存空间紧张而对时间效率要求不高，可以增加负载因子loadFactor的值，这个值可以大于1。



源码阅读：

- **threshold：表示容器所能容纳的 key-value 对极限。**
- **loadFactor：负载因子。**
- **modCount：记录修改次数。**
- **size：表示实际存在的键值对数量。**
- **table：一个哈希桶数组，键值对就存放在里面。**

```java
public class HashMap<K,V> extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable {

	//所能容纳的key-value对极限
	int threshold;

	//负载因子
	final float loadFactor;

	//记录修改次数
	int modCount;

	//实际存在的键值对数量
	int size;

	//哈希桶数组
	transient Node<K,V>[] table;
}
```

1.确定索引位置：

![image-20200506210900063](https://github.com/rainluacgq/java/blob/master/java%E5%9F%BA%E7%A1%80/pic/image-20200506210900063.png)

```java
方法一：
static final int hash(Object key) {   //jdk1.8 & jdk1.7
     int h;
     // h = key.hashCode() 为第一步 取hashCode值
     // h ^ (h >>> 16)  为第二步 高位参与运算
     return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
方法二：
static int indexFor(int h, int length) {  //jdk1.7的源码，jdk1.8没有这个方法，但是实现原理一样的
     return h & (length-1);  //第三步 取模运算
}
```

2.put

```java
/**
 \* put方法
 */
public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
}
```

```java
/**
 \* 插入元素方法
 */
 final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
		//1、判断数组table是否为空或为null
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
		//2、判断数组下标table[i]==null
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
        else {
            Node<K,V> e; K k;
			//3、判断table[i]的首个元素是否和传入的key一样
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
			//4、判断table[i] 是否为treeNode
            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
				//5、遍历table[i]，判断链表长度是否大于8
                for (int binCount = 0; ; ++binCount) {
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
						//长度大于8，转红黑树结构
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
			//传入的K元素已经存在，直接覆盖value
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
		//6、判断size是否超出最大容量
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
}
```

RBTree的定义如下:

1. 任何一个节点都有颜色，黑色或者红色
2. 根节点是黑色的
3. 父子节点之间不能出现两个连续的红节点
4. 任何一个节点向下遍历到其子孙的叶子节点，所经过的黑节点个数必须相等
5. 空节点被认为是黑色的

RBTree在理论上还是一棵BST树，但是它在对BST的插入和删除操作时会维持树的平衡，即保证树的高度在[logN,logN+1]（理论上，极端的情况下可以出现RBTree的高度达到2*logN，但实际上很难遇到）。这样RBTree的查找时间复杂度始终保持在O(logN)从而接近于理想的BST。RBTree的删除和插入操作的时间复杂度也是O(logN)。RBTree的查找操作就是BST的查找操作。

参考：https://zhuanlan.zhihu.com/p/24367771

在线生成红黑树：https://www.cs.usfca.edu/~galles/visualization/RedBlack.html

3.扩容

```java
/**
  \* JDK1.7扩容方法
  \* 传入新的容量
  */
void resize(int newCapacity) {
    //引用扩容前的Entry数组
    Entry[] oldTable = table;
    int oldCapacity = oldTable.length;
	//扩容前的数组大小如果已经达到最大(2^30)了
    if (oldCapacity == MAXIMUM_CAPACITY) {
		//修改阈值为int的最大值(2^31-1)，这样以后就不会扩容了
        threshold = Integer.MAX_VALUE;
        return;
    }
	//初始化一个新的Entry数组
    Entry[] newTable = new Entry[newCapacity];
	//将数据转移到新的Entry数组里，这里包含最重要的重新定位
    transfer(newTable);
	//HashMap的table属性引用新的Entry数组
    table = newTable;
    threshold = (int) (newCapacity * loadFactor);//修改阈值
}
```

```java
//遍历每个元素，按新的容量进行rehash，放到新的数组上
void transfer(Entry[] newTable) {
	//src引用了旧的Entry数组
    Entry[] src = table;
    int newCapacity = newTable.length;
    for (int j = 0; j < src.length; j++) {
		//遍历旧的Entry数组
        Entry<K, V> e = src[j];
		//取得旧Entry数组的每个元素
        if (e != null) {
			//释放旧Entry数组的对象引用（for循环后，旧的Entry数组不再引用任何对象）
            src[j] = null;
            do {
                Entry<K, V> next = e.next;
				//重新计算每个元素在数组中的位置
				//实现逻辑，也是上文那个取模运算方法
                int i = indexFor(e.hash, newCapacity);
				//标记数组
                e.next = newTable[i];
				//将元素放在数组上
                newTable[i] = e;
				//访问下一个Entry链上的元素，循环遍历
                e = next;
            } while (e != null);
        }
    }
}
```

阿里巴巴java开发规范推荐

【推荐】 高度注意 Map 类集合 K/V 能不能存储 null 值的情况，如下表格：

| 集合类            | Key           | Value         | Super       | 说明                    |
| ----------------- | ------------- | ------------- | ----------- | ----------------------- |
| Hashtable         | 不允许为 null | 不允许为 null | Dictionary  | 线程安全                |
| ConcurrentHashMap | 不允许为 null | 不允许为 null | AbstractMap | 锁分段技术（ JDK8:CAS） |
| TreeMap           | 不允许为 null | 允许为 null   | AbstractMap | 线程不安全              |
| HashMap           | 允许为 null   | 允许为 null   | AbstractMap | 线程不安全              |

【参考】  HashMap 在容量不够进行 resize 时由于高并发可能出现死链，导致 CPU 飙升，在
开发过程中注意规避此风险。  

参考：https://zhuanlan.zhihu.com/p/21673805