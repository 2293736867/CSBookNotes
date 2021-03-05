# Table of Contents

* [1 来源](#1-来源)
* [2 `GC`日志：`-Xlog:gc`](#2-gc日志-xloggc)
* [3 系统参数打印](#3-系统参数打印)
* [4 堆参数](#4-堆参数)
  * [4.1 最大堆与初始堆参数](#41-最大堆与初始堆参数)
  * [4.2 新生代参数](#42-新生代参数)
  * [4.3 堆溢出处理](#43-堆溢出处理)
* [5 非堆参数](#5-非堆参数)
  * [5.1 方法区](#51-方法区)
  * [5.2 栈](#52-栈)
  * [5.3 直接内存](#53-直接内存)

# 1 来源

- 来源：《Java虚拟机 JVM故障诊断与性能优化》——葛一鸣
- 章节：第三章

本文是第三章的一些笔记整理。

# 2 `GC`日志：`-Xlog:gc`
要打印`GC`日志的话，可以加上`-Xlog:gc`参数（`JDK8`及以下请使用`-XX:+PrintGC`），开启`GC`打印后，每次`GC`就会打印如下的日志（`OpenJDK11 -Xlog:gc`）：

```bash
[0.126s][info][gc] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 25M->0M(502M) 1.902ms
[0.205s][info][gc] GC(1) Pause Young (Normal) (G1 Evacuation Pause) 300M->0M(502M) 4.174ms
[0.236s][info][gc] GC(2) Pause Young (Normal) (G1 Evacuation Pause) 300M->0M(502M) 2.067ms
[0.268s][info][gc] GC(3) Pause Young (Normal) (G1 Evacuation Pause) 300M->0M(502M) 2.362ms
```
其中开头的时间表示发生`GC`的时刻，`25M->0M(502M)`表示：

- `GC`前，堆使用量为`25M`
- `GC`后，堆使用量为`0M`
- 堆空间总和约为`502M`

末尾的时间表示本次`GC`的耗时。

另外如果需要更加详细的参数，可以使用`-Xlog:gc*`（`JDK8`及以下请使用`-XX:+PrintGCDetails`），比如下面是一部分的`GC`日志（`-Xlog:gc*`）：
```bash
[0.137s][info][gc,start     ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
[0.138s][info][gc,task      ] GC(0) Using 10 workers of 10 for evacuation
[0.147s][info][gc,phases    ] GC(0)   Pre Evacuate Collection Set: 0.0ms
[0.147s][info][gc,phases    ] GC(0)   Evacuate Collection Set: 8.8ms
[0.147s][info][gc,phases    ] GC(0)   Post Evacuate Collection Set: 0.2ms
[0.147s][info][gc,phases    ] GC(0)   Other: 0.8ms
[0.147s][info][gc,heap      ] GC(0) Eden regions: 25->0(300)
[0.147s][info][gc,heap      ] GC(0) Survivor regions: 0->1(4)
[0.147s][info][gc,heap      ] GC(0) Old regions: 0->0
[0.147s][info][gc,heap      ] GC(0) Humongous regions: 0->0
[0.147s][info][gc,metaspace ] GC(0) Metaspace: 6633K->6633K(1056768K)
[0.147s][info][gc           ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 25M->0M(502M) 9.878ms
[0.147s][info][gc,cpu       ] GC(0) User=0.05s Sys=0.00s Real=0.01s
```
- 行首的时间：事件发生的时刻
- `GC(0)`：这是第1次`GC`，接着会有`GC(1)`、`GC(2)`
- `Pause Young（Normal）`：这次`GC`回收了新生代
- `Using 10 workers`：使用10个工作线程
- `Pre Evacuate Collection Set`/`Evacuate Collection Set`/`Post Evacuate`/`Other`：表示`G1`垃圾回收标记，清除算法不同阶段所花费的时间
- `Eden/Survivor/Old/Humongous/Metaspace`：分别表示`eden区`、`存活区`、`老年区`、`巨型对象区`（就是很大很大的对象所在的区域）、`元数据区`在`GC`前后的大小
- `25M-0M(502M)`：`GC`前堆占用`25M`，`GC`后为`0M`，可用堆空间为`502M`
- `User/Sys/Real`：分别表示`用户态CPU耗时`、`系统CPU耗时`、`GC真实经历时间`

如果想查看更全面的堆信息，可以使用`Visual VM`，将在后续文章中叙述。

另外如果需要将日志持久化，可以使用`-Xlog:gc:gc.log`。

# 3 系统参数打印
参数`-XX:+PrintVMOptinos`可以打印运行时接收到的显式参数，而`-XX:+PrintCommandLineFlags`可以打印传递给`JVM`的隐式与显式参数：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210304094815957.png)

另外一个参数是`-XX:+PrintFlagsFinal`，会打印所有系统参数的值（数量很多）：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210304094936823.png)

# 4 堆参数
## 4.1 最大堆与初始堆参数
`Java`进程启动时，虚拟机就会分配一块初始堆空间，可以使用参数`-Xms`指定这块空间的初始化大小。一般来说虚拟机会尽可能维持在初始堆空间范围内运行，但是如果初始堆空间耗尽，虚拟机会将堆空间进行扩展，扩展上限为最大堆空间，最大堆空间可以使用参数`-Xmx`指定。

来一段代码测试一下：
```java
public class Main {

    public static void main(String[] args){
        printMemory();

        byte [] bytes = new byte[1*1024*1024];
        System.out.println("Allocate 1024 KB array");
        printMemory();

        bytes = new byte[4*1024*1024];
        System.out.println("Allocate 4096 KB array");
        printMemory();
    }

    public static void printMemory(){
        System.out.println();
        System.out.println("Max memory = " + Runtime.getRuntime().maxMemory() / 1024+ " KB");
        System.out.println("Free memory = "+Runtime.getRuntime().freeMemory()/1024+ " KB");
        System.out.println("Total memory = " + Runtime.getRuntime().totalMemory()/ 1024+ " KB");
        System.out.println();
    }
}
```
参数：
```bash
-Xmx20m
-Xms5m
-XX:+PrintCommandLineFlags
-Xlog:gc*
-XX:+UseSerialGC
```
输出：
```bash
-XX:InitialHeapSize=5242880 -XX:MaxHeapSize=20971520 -XX:+PrintCommandLineFlags -XX:+PrintGCDetails -XX:ReservedCodeCacheSize=251658240 -XX:+SegmentedCodeCache -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseSerialGC 
[0.002s][info   ][gc] Using Serial
[0.002s][info   ][gc,heap,coops] Heap address: 0x00000000fec00000, size: 20 MB, Compressed Oops mode: 32-bit

[0.110s][info   ][gc,start     ] GC(0) Pause Young (Allocation Failure)
[0.112s][info   ][gc,heap      ] GC(0) DefNew: 1664K->192K(1856K)
[0.112s][info   ][gc,heap      ] GC(0) Tenured: 0K->598K(4096K)
[0.112s][info   ][gc,metaspace ] GC(0) Metaspace: 6436K->6436K(1056768K)
[0.112s][info   ][gc           ] GC(0) Pause Young (Allocation Failure) 1M->0M(5M) 2.069ms
[0.112s][info   ][gc,cpu       ] GC(0) User=0.00s Sys=0.00s Real=0.01s
Max memory = 19840 KB
Free memory = 4797 KB
Total memory = 5952 KB

Allocate 1024 KB array

Max memory = 19840 KB
Free memory = 3773 KB
Total memory = 5952 KB

[0.128s][info   ][gc,start     ] GC(1) Pause Young (Allocation Failure)
[0.129s][info   ][gc,start     ] GC(2) Pause Full (Allocation Failure)
[0.129s][info   ][gc,phases,start] GC(2) Phase 1: Mark live objects
[0.130s][info   ][gc,phases      ] GC(2) Phase 1: Mark live objects 1.366ms
[0.130s][info   ][gc,phases,start] GC(2) Phase 2: Compute new object addresses
[0.130s][info   ][gc,phases      ] GC(2) Phase 2: Compute new object addresses 0.235ms
[0.130s][info   ][gc,phases,start] GC(2) Phase 3: Adjust pointers
[0.131s][info   ][gc,phases      ] GC(2) Phase 3: Adjust pointers 0.624ms
[0.131s][info   ][gc,phases,start] GC(2) Phase 4: Move objects
[0.131s][info   ][gc,phases      ] GC(2) Phase 4: Move objects 0.042ms
[0.131s][info   ][gc             ] GC(2) Pause Full (Allocation Failure) 1M->1M(5M) 2.335ms
[0.131s][info   ][gc,heap        ] GC(1) DefNew: 1579K->0K(1856K)
[0.131s][info   ][gc,heap        ] GC(1) Tenured: 598K->1899K(4096K)
[0.131s][info   ][gc,metaspace   ] GC(1) Metaspace: 6624K->6624K(1056768K)
[0.131s][info   ][gc             ] GC(1) Pause Young (Allocation Failure) 2M->1M(5M) 3.636ms
[0.131s][info   ][gc,cpu         ] GC(1) User=0.00s Sys=0.01s Real=0.00s
Allocate 4096 KB array

Max memory = 19840 KB
Free memory = 4087 KB
Total memory = 10116 KB

[0.133s][info   ][gc,heap,exit   ] Heap
[0.133s][info   ][gc,heap,exit   ]  def new generation   total 1920K, used 44K [0x00000000fec00000, 0x00000000fee10000, 0x00000000ff2a0000)
[0.133s][info   ][gc,heap,exit   ]   eden space 1728K,   2% used [0x00000000fec00000, 0x00000000fec0b198, 0x00000000fedb0000)
[0.133s][info   ][gc,heap,exit   ]   from space 192K,   0% used [0x00000000fedb0000, 0x00000000fedb0000, 0x00000000fede0000)
[0.133s][info   ][gc,heap,exit   ]   to   space 192K,   0% used [0x00000000fede0000, 0x00000000fede0000, 0x00000000fee10000)
[0.133s][info   ][gc,heap,exit   ]  tenured generation   total 8196K, used 5995K [0x00000000ff2a0000, 0x00000000ffaa1000, 0x0000000100000000)
[0.133s][info   ][gc,heap,exit   ]    the space 8196K,  73% used [0x00000000ff2a0000, 0x00000000ff87aed0, 0x00000000ff87b000, 0x00000000ffaa1000)
[0.133s][info   ][gc,heap,exit   ]  Metaspace       used 6640K, capacity 6723K, committed 7040K, reserved 1056768K
[0.133s][info   ][gc,heap,exit   ]   class space    used 590K, capacity 623K, committed 640K, reserved 1048576K
```
最大内存由`-XX:MaxHeapSize`指定，该值为`-Xmx`的值，也就是`20 * 1024 * 1024`，而打印的最大可用内存为`20316160`，比设定的值少，这是因为分配给堆的内存空间与实际可用的内存空间并不是同一个概念，由于`GC`的需要，虚拟机会对堆空间进行分区管理，不同的区会采用不同的回收算法，一些算法会使用空间换时间的策略，因此会存在损失，最终的结果是实际可用内存会浪费大小等于`from`/`to`的空间，从输出可以知道：
```bash
[0.139s][info][gc,heap,exit   ]   from space 192K,   0% used [0x00000000fedb0000, 0x00000000fedb0000, 0x00000000fede0000)
```
`from`的大小为`192k`，但是实际情况是最大可用内存`19840k`+`from`的`192k`=`20032k`，并不是分配的内存`20480k`，这是因为虚拟机会对`from`/`to`进行对齐，将最大可用内存加上对齐后的`from`/`to`即得到分配的内存大小。

另外，打印显示，初始运行空闲内存`4797k`，分配一个`1024k`数组后，空闲内存为`3773k`，正好符合，接着分配`4096k`，因为内存不足，对堆空间进行扩展后再分配，扩展后的堆大小为`10116k`。

在实际工作中，可以将`-Xms`与`-Xmx`设置为相同，这样可以减少运行时的`GC`次数，提高性能。

## 4.2 新生代参数
`-Xmn`可以设置新生代的大小，设置一个较大的新生代会减小老年代的大小，这个参数堆`GC`有很大影响，一般设置为堆空间的`1/3-1/4`。参数`-XX:SurvivorRatio`，也就是幸存区比例，可用来设置`eden区`与`from/to区`的比例，相当于：
```bash
-XX:SurvivorRatio=eden/from=eden/to
```
一个简单的例子如下：
```java
public static void main(String[] args){
    byte [] b = null;
    for (int i = 0; i < 10; i++) {
        b = new byte[1*1024*1024] ;
    }
}
```
参数：
```bash
-Xmx20m
-Xms20m
-Xmn1m
-XX:SurvivorRatio=2
-Xlog:gc*
-XX:+UseSerialGC
```
输出：
```bash
[0.002s][info][gc] Using Serial
[0.002s][info][gc,heap,coops] Heap address: 0x00000000fec00000, size: 20 MB, Compressed Oops mode: 32-bit
[0.042s][info][gc,start     ] GC(0) Pause Young (Allocation Failure)
[0.044s][info][gc,heap      ] GC(0) DefNew: 512K->256K(768K)
[0.044s][info][gc,heap      ] GC(0) Tenured: 0K->172K(19456K)
[0.044s][info][gc,metaspace ] GC(0) Metaspace: 3871K->3871K(1056768K)
[0.044s][info][gc           ] GC(0) Pause Young (Allocation Failure) 0M->0M(19M) 1.617ms
[0.044s][info][gc,cpu       ] GC(0) User=0.01s Sys=0.00s Real=0.00s
[0.064s][info][gc,start     ] GC(1) Pause Young (Allocation Failure)
[0.065s][info][gc,heap      ] GC(1) DefNew: 767K->76K(768K)
[0.065s][info][gc,heap      ] GC(1) Tenured: 172K->425K(19456K)
[0.065s][info][gc,metaspace ] GC(1) Metaspace: 4518K->4518K(1056768K)
[0.065s][info][gc           ] GC(1) Pause Young (Allocation Failure) 0M->0M(19M) 0.870ms
[0.065s][info][gc,cpu       ] GC(1) User=0.00s Sys=0.00s Real=0.00s
[0.093s][info][gc,heap,exit ] Heap
[0.093s][info][gc,heap,exit ]  def new generation   total 768K, used 562K [0x00000000fec00000, 0x00000000fed00000, 0x00000000fed00000)
[0.093s][info][gc,heap,exit ]   eden space 512K,  94% used [0x00000000fec00000, 0x00000000fec79730, 0x00000000fec80000)
[0.093s][info][gc,heap,exit ]   from space 256K,  29% used [0x00000000fec80000, 0x00000000fec93260, 0x00000000fecc0000)
[0.093s][info][gc,heap,exit ]   to   space 256K,   0% used [0x00000000fecc0000, 0x00000000fecc0000, 0x00000000fed00000)
[0.093s][info][gc,heap,exit ]  tenured generation   total 19456K, used 10665K [0x00000000fed00000, 0x0000000100000000, 0x0000000100000000)
[0.093s][info][gc,heap,exit ]    the space 19456K,  54% used [0x00000000fed00000, 0x00000000ff76a630, 0x00000000ff76a800, 0x0000000100000000)
[0.093s][info][gc,heap,exit ]  Metaspace       used 6190K, capacity 6251K, committed 6528K, reserved 1056768K
[0.093s][info][gc,heap,exit ]   class space    used 535K, capacity 570K, committed 640K, reserved 1048576K
```
`eden区`与`from区`的比值为`2:1`，因此`eden区`为`512K`，总可用新生代大小为`512K+256K=768K`，新生代总大小为`512K+256K+256K=1M`，由于`eden`区无法容纳分配`1MB`数组，因此触发了新生代`GC`，所有数组分配在了老年代。

而如果使用`-Xmn7m`（其他参数保持不变），输出如下：
```bash
[0.003s][info][gc] Using Serial
[0.003s][info][gc,heap,coops] Heap address: 0x00000000fec00000, size: 20 MB, Compressed Oops mode: 32-bit
[0.096s][info][gc,start     ] GC(0) Pause Young (Allocation Failure)
[0.097s][info][gc,heap      ] GC(0) DefNew: 2684K->1752K(5376K)
[0.097s][info][gc,heap      ] GC(0) Tenured: 0K->0K(13312K)
[0.097s][info][gc,metaspace ] GC(0) Metaspace: 5929K->5929K(1056768K)
[0.097s][info][gc           ] GC(0) Pause Young (Allocation Failure) 2M->1M(18M) 1.350ms
[0.097s][info][gc,cpu       ] GC(0) User=0.00s Sys=0.00s Real=0.00s
[0.098s][info][gc,start     ] GC(1) Pause Young (Allocation Failure)
[0.099s][info][gc,heap      ] GC(1) DefNew: 4928K->1024K(5376K)
[0.099s][info][gc,heap      ] GC(1) Tenured: 0K->727K(13312K)
[0.099s][info][gc,metaspace ] GC(1) Metaspace: 5996K->5996K(1056768K)
[0.099s][info][gc           ] GC(1) Pause Young (Allocation Failure) 4M->1M(18M) 1.142ms
[0.099s][info][gc,cpu       ] GC(1) User=0.01s Sys=0.00s Real=0.00s
[0.100s][info][gc,start     ] GC(2) Pause Young (Allocation Failure)
[0.100s][info][gc,heap      ] GC(2) DefNew: 4180K->1024K(5376K)
[0.100s][info][gc,heap      ] GC(2) Tenured: 727K->728K(13312K)
[0.100s][info][gc,metaspace ] GC(2) Metaspace: 6008K->6008K(1056768K)
[0.100s][info][gc           ] GC(2) Pause Young (Allocation Failure) 4M->1M(18M) 0.190ms
[0.100s][info][gc,cpu       ] GC(2) User=0.00s Sys=0.00s Real=0.00s
[0.100s][info][gc,heap,exit ] Heap
[0.100s][info][gc,heap,exit ]  def new generation   total 5376K, used 4211K [0x00000000fec00000, 0x00000000ff300000, 0x00000000ff300000)
[0.100s][info][gc,heap,exit ]   eden space 3584K,  88% used [0x00000000fec00000, 0x00000000fef1cc00, 0x00000000fef80000)
[0.100s][info][gc,heap,exit ]   from space 1792K,  57% used [0x00000000ff140000, 0x00000000ff2402a0, 0x00000000ff300000)
[0.100s][info][gc,heap,exit ]   to   space 1792K,   0% used [0x00000000fef80000, 0x00000000fef80000, 0x00000000ff140000)
[0.100s][info][gc,heap,exit ]  tenured generation   total 13312K, used 728K [0x00000000ff300000, 0x0000000100000000, 0x0000000100000000)
[0.100s][info][gc,heap,exit ]    the space 13312K,   5% used [0x00000000ff300000, 0x00000000ff3b61f8, 0x00000000ff3b6200, 0x0000000100000000)
[0.100s][info][gc,heap,exit ]  Metaspace       used 6034K, capacity 6091K, committed 6272K, reserved 1056768K
[0.100s][info][gc,heap,exit ]   class space    used 518K, capacity 538K, committed 640K, reserved 1048576K
```
此参数下，`eden区`有足够的空间，所有数组分配在`eden`区，但是不足以预留`10M`空间，因此产生了`GC`，每次申请空间也废弃了上一次申请的空间，在新生代`GC`中有效回收了这些内存，最后的结果是所有内存分配都在新生代进行，只是在`GC`过程中部分新生代对象晋升到了老年代。

再次增大新生代，使用`-Xmn15m -XX:SurvivorRatio=8`（其他参数不变），输出如下：
```bash
[0.003s][info][gc] Using Serial
[0.003s][info][gc,heap,coops] Heap address: 0x00000000fec00000, size: 20 MB, Compressed Oops mode: 32-bit
start
[0.097s][info][gc,start     ] GC(0) Pause Young (Allocation Failure)
[0.099s][info][gc,heap      ] GC(0) DefNew: 11416K->1471K(13696K)
[0.099s][info][gc,heap      ] GC(0) Tenured: 0K->294K(5312K)
[0.099s][info][gc,metaspace ] GC(0) Metaspace: 6103K->6103K(1056768K)
[0.099s][info][gc           ] GC(0) Pause Young (Allocation Failure) 11M->1M(18M) 2.322ms
[0.099s][info][gc,cpu       ] GC(0) User=0.00s Sys=0.00s Real=0.01s
end
[0.099s][info][gc,heap,exit ] Heap
[0.099s][info][gc,heap,exit ]  def new generation   total 13696K, used 2934K [0x00000000fec00000, 0x00000000ffad0000, 0x00000000ffad0000)
[0.099s][info][gc,heap,exit ]   eden space 12224K,  11% used [0x00000000fec00000, 0x00000000fed6d908, 0x00000000ff7f0000)
[0.099s][info][gc,heap,exit ]   from space 1472K,  99% used [0x00000000ff960000, 0x00000000ffacfff8, 0x00000000ffad0000)
[0.099s][info][gc,heap,exit ]   to   space 1472K,   0% used [0x00000000ff7f0000, 0x00000000ff7f0000, 0x00000000ff960000)
[0.099s][info][gc,heap,exit ]  tenured generation   total 5312K, used 294K [0x00000000ffad0000, 0x0000000100000000, 0x0000000100000000)
[0.099s][info][gc,heap,exit ]    the space 5312K,   5% used [0x00000000ffad0000, 0x00000000ffb19960, 0x00000000ffb19a00, 0x0000000100000000)
[0.099s][info][gc,heap,exit ]  Metaspace       used 6164K, capacity 6251K, committed 6528K, reserved 1056768K
[0.099s][info][gc,heap,exit ]   class space    used 532K, capacity 570K, committed 640K, reserved 1048576K
```
可以看到新生代使用`15M`空间，`eden区`占了`12288K`，完全满足了`10MB`需要，并没有发生`GC`（日志的`GC`只是在`for`循环结束后产生的，一次性回收了`10M`）。

实际工作中，应根据系统的特点，做合理的设置，基本策略是：

- 尽可能将对象预留在新生代
- 减少老年代`GC`次数

另外，可以使用`-XX:NewRatio=老年代/新生代`指定新生代和老年代的比例，比如使用参数：
```bash
-Xmx20m
-Xms20m
-XX:NewRatio=2
-Xlog:gc*
-XX:+UseSerialGC
```
输出：
```bash
[0.005s][info][gc] Using Serial
[0.005s][info][gc,heap,coops] Heap address: 0x00000000fec00000, size: 20 MB, Compressed Oops mode: 32-bit
[0.096s][info][gc,start     ] GC(0) Pause Young (Allocation Failure)
[0.097s][info][gc,heap      ] GC(0) DefNew: 4852K->639K(6144K)
[0.097s][info][gc,heap      ] GC(0) Tenured: 0K->1112K(13696K)
[0.097s][info][gc,metaspace ] GC(0) Metaspace: 5905K->5905K(1056768K)
[0.097s][info][gc           ] GC(0) Pause Young (Allocation Failure) 4M->1M(19M) 1.413ms
[0.097s][info][gc,cpu       ] GC(0) User=0.00s Sys=0.00s Real=0.00s
[0.098s][info][gc,start     ] GC(1) Pause Young (Allocation Failure)
[0.099s][info][gc,heap      ] GC(1) DefNew: 5920K->0K(6144K)
[0.099s][info][gc,heap      ] GC(1) Tenured: 1112K->2776K(13696K)
[0.099s][info][gc,metaspace ] GC(1) Metaspace: 5970K->5970K(1056768K)
[0.099s][info][gc           ] GC(1) Pause Young (Allocation Failure) 6M->2M(19M) 1.129ms
[0.099s][info][gc,cpu       ] GC(1) User=0.00s Sys=0.01s Real=0.00s
[0.100s][info][gc,heap,exit ] Heap
[0.100s][info][gc,heap,exit ]  def new generation   total 6144K, used 2238K [0x00000000fec00000, 0x00000000ff2a0000, 0x00000000ff2a0000)
[0.100s][info][gc,heap,exit ]   eden space 5504K,  40% used [0x00000000fec00000, 0x00000000fee2f690, 0x00000000ff160000)
[0.100s][info][gc,heap,exit ]   from space 640K,   0% used [0x00000000ff160000, 0x00000000ff160398, 0x00000000ff200000)
[0.100s][info][gc,heap,exit ]   to   space 640K,   0% used [0x00000000ff200000, 0x00000000ff200000, 0x00000000ff2a0000)
[0.100s][info][gc,heap,exit ]  tenured generation   total 13696K, used 2776K [0x00000000ff2a0000, 0x0000000100000000, 0x0000000100000000)
[0.100s][info][gc,heap,exit ]    the space 13696K,  20% used [0x00000000ff2a0000, 0x00000000ff556250, 0x00000000ff556400, 0x0000000100000000)
[0.100s][info][gc,heap,exit ]  Metaspace       used 5998K, capacity 6091K, committed 6272K, reserved 1056768K
[0.100s][info][gc,heap,exit ]   class space    used 517K, capacity 538K, committed 640K, reserved 1048576K
```
堆大小为`20M`，新生代和老年代的比为`1:2`，因此新生代大小约为`7M`，老年代为`13M`，分配`1M`时，由于`from/to`空间不足，导致两个`1MB`的数组进入了老年代。

## 4.3 堆溢出处理
如果在`Java`程序运行过程中，堆空间不足，会抛出内存溢出错误，也就是常见的`OOM`。想要分析原因，可以使用参数`-XX:+HeapDumpOnOutOfMemoryError`，可以在内存溢出时导出整个堆的信息，配合使用的还有`-XX:HeapDumpPath`，指定导出堆的存放路径，例子如下：

```java
public static void main(String[] args){
    List<byte[]> list = new ArrayList<>();
    for (int i = 0; i < 25; i++) {
        list.add(new byte[1*1024*1024]);
    }
}
```
参数：
```bash
-Xmx20m
-Xms5m
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=out.dump
```
例子分配了`25M`的内存，但是堆只有`20M`，会抛出`OOM`，并且文件保存到`out.dump`中，注意该文件是二进制文件，需要使用专业的工具（如`MAT`等）查看。

# 5 非堆参数
## 5.1 方法区
从`JDK8`开始，永久区被移除，使用了新的元数据区来存放类的元数据，默认情况下，元数据区受系统可用内存的限制，但是仍然可以使用`-XX:MaxMetaspaceSize`指定永久区的最大可用值。

## 5.2 栈
栈是每个线程私有的空间，可以使用`-Xss`指定线程的栈大小，具体在[笔者之前的文章中](https://blog.csdn.net/qq_27525611/article/details/114308179?spm=1001.2014.3001.5501)。

## 5.3 直接内存
直接内存跳过了`Java堆`，可以使得程序直接访问原生堆空间，在一定程度上加快了内存空间的访问速度。最大可用直接内存可以使用`-XX:MaxDirectMemorySize`设置，如果不设置，默认为最大堆空间，即`-Xmx`的值，当直接内存使用量到达最大值时，会触发`GC`，如果`GC`后不能有效释放足够的空间，直接内存依然会引起系统的`OOM`。

下面测试一下直接内存与堆的速度：
```java
public class Main {

    public static final int count = 1000000;

    public static void directAccess(){
        long start = System.currentTimeMillis();
        ByteBuffer b = ByteBuffer.allocateDirect(500);
        for(int i=0;i<count;++i){
            for (int j = 0; j < 99; j++) {
                b.putInt(j) ;
            }
            b.flip();
            for (int j = 0; j < 99; j++) {
                b.getInt();
            }
            b.clear();
        }
        long end = System.currentTimeMillis();
        System.out.println("Direct access: "+(end-start)+" ms");
    }

    public static void bufferAccess(){
        long start = System.currentTimeMillis();
        ByteBuffer b = ByteBuffer.allocate(500);
        for(int i=0;i<count;++i){
            for (int j = 0; j < 99; j++) {
                b.putInt(j) ;
            }
            b.flip();
            for (int j = 0; j < 99; j++) {
                b.getInt();
            }
            b.clear();
        }
        long end = System.currentTimeMillis();
        System.out.println("Buffer access: "+(end-start)+" ms");
    }


    public static void main(String[] args){
        directAccess();
        bufferAccess();

        directAccess();
        bufferAccess();
    }
}
```
输出（不带任何参数）：
```bash
Direct access: 167 ms
Buffer access: 70 ms
Direct access: 176 ms
Buffer access: 67 ms
```
直接内存的访问速度要快于堆内存，但是有一个缺点就是申请的时候速度慢：
```java
public static void directAllocate(){
    long start = System.currentTimeMillis();
    for (int i = 0; i < count; i++) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1000);
    }
    long end = System.currentTimeMillis();
    System.out.println("Direct allocate: "+(end-start)+" ms");
}

public static void bufferAllocate(){
    long start = System.currentTimeMillis();
    for (int i = 0; i < count; i++) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1000);
    }
    long end = System.currentTimeMillis();
    System.out.println("Buffer allocate: "+(end-start)+" ms");
}


public static void main(String[] args){
    directAllocate();
    bufferAllocate();

    directAllocate();
    bufferAllocate();
}
```
输出：
```bash
Direct allocate: 867 ms
Buffer allocate: 287 ms
Direct allocate: 676 ms
Buffer allocate: 208 ms
```
简单来说，直接内存适合申请次数较少、访问较频繁的场合，如果需要频繁申请内存空间，并不适合使用直接内存。
