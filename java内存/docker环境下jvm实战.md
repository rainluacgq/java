### docker环境下的JVM实战

1.查看JVM线程

```bash
docker exec -it [containerID] /bin/bash
```

![image-20200518201102486](https://github.com/rainluacgq/java/blob/master/java内存/pic/image-20200518201102486.png)

执行jstack指令

```bash
jstack 1
```

![image-20200518201857178](https://github.com/rainluacgq/java/blob/master/java内存/pic/image-20200518201857178.png)

jmap 查看对象在堆中的分布

![image-20200606190813484](https://github.com/rainluacgq/java/blob/master/java内存/pic/image_20200606191217.png)

查看GC概况

![image-20200518202141319](https://github.com/rainluacgq/java/blob/master/java内存/pic/image-20200518202141319.png)

查看JVM运行时参数

![image-20200518202638626](https://github.com/rainluacgq/java/blob/master/java内存/pic/image-20200518202638626.png)

JVM默认配置：

```
VM Flags:
-XX:CICompilerCount=3 -XX:ConcGCThreads=2 -XX:G1ConcRefinementThreads=6 -XX:G1HeapRegionSize=1048576 -XX:GCDrainStackTargetSize=64 -XX:InitialHeapSize=130023424 -XX:MarkStackSize=4194304 -XX:MaxHeapSize=2057306112 -XX:MaxNewSize=1234173952 -XX:MinHeapDeltaBytes=1048576 -XX:MinHeapSize=8388608 -XX:NonNMethodCodeHeapSize=5832780 -XX:NonProfiledCodeHeapSize=122912730 -XX:ProfiledCodeHeapSize=122912730 -XX:ReservedCodeCacheSize=251658240 -XX:+SegmentedCodeCache -XX:SoftMaxHeapSize=2057306112 -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseFastUnorderedTimeStamps -XX:+UseG1GC
```

