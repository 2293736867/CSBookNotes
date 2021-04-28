# Table of Contents

* [1 来源](#1-来源)
* [2 概述](#2-概述)
* [3 串行回收器](#3-串行回收器)
* [4 并行回收器](#4-并行回收器)
  * [4.1 `ParNew`](#41-parnew)
  * [4.2 `ParallelGC`](#42-parallelgc)
  * [4.3 `ParallelOldGC`](#43-paralleloldgc)
* [5 `CMS`](#5-cms)
  * [5.1 工作流程](#51-工作流程)
  * [5.2 主要参数](#52-主要参数)
* [6 `G1`](#6-g1)
  * [6.1 `G1`工作流程](#61-g1工作流程)
    * [6.1.1 新生代`GC`](#611-新生代gc)
    * [6.1.2 并发标记周期](#612-并发标记周期)
    * [6.1.3 混合回收](#613-混合回收)
    * [6.1.4 `Full GC`](#614-full-gc)
  * [6.2 `G1`相关参数](#62-g1相关参数)
* [7 `GC`调优简单实验](#7-gc调优简单实验)
  * [7.1 概述](#71-概述)
  * [7.2 步骤](#72-步骤)
    * [7.2.1 添加线程组](#721-添加线程组)
    * [7.2.2 添加采样器](#722-添加采样器)
    * [7.2.3 添加总结报告](#723-添加总结报告)
  * [7.3 测试](#73-测试)
  * [7.4 调优](#74-调优)
* [8 附录一：回收的一些细节讨论](#8-附录一回收的一些细节讨论)
  * [8.1 禁用显式`GC`](#81-禁用显式gc)
  * [8.2 显式`GC`使用并发回收](#82-显式gc使用并发回收)
  * [8.3 关于对象如何晋升到老年代](#83-关于对象如何晋升到老年代)
  * [8.4 关于`TLAB`](#84-关于tlab)
    * [8.4.1 一个简单的测试](#841-一个简单的测试)
    * [8.4.2 对象的分配](#842-对象的分配)
* [9 附录二： 常用`GC`参数总结](#9-附录二-常用gc参数总结)
  * [9.1 串行回收器相关参数](#91-串行回收器相关参数)
  * [9.2 并行回收器相关参数](#92-并行回收器相关参数)
  * [9.3 `CMS`相关参数](#93-cms相关参数)
  * [9.4 `G1`相关参数](#94-g1相关参数)
  * [9.5 `TLAB`相关参数](#95-tlab相关参数)
  * [9.6 其他参数](#96-其他参数)
* [10 参考](#10-参考)


# 1 来源
- 来源：《Java虚拟机 JVM故障诊断与性能优化》——葛一鸣
- 章节：第五章

本文是第五章的一些笔记整理。

# 2 概述
本文主要讲述了`JVM`中的常见垃圾回收器，包括：

- 串行回收器
- 并行回收器
- `CMS`
- `G1`

另外还提及了内存分配的一些细节以及一个简单的`JVM`调优实战。

# 3 串行回收器
串行回收器是指使用单线程进行垃圾回收的回收器，每次回收时，串行回收器只有一个工作线程。串行回收器作为最古老的一种回收器，特点如下：

- 仅仅使用单线程进行垃圾回收
- 独占式的垃圾回收方式

在串行回收器进行垃圾回收的时候，应用线程需要暂停工作直到回收完成，这种现象就是著名的`Stop-The-World`，也就是`STW`。

串行回收器的相关参数如下：

- `-XX:+UseSerialGC`：新生代与老年代都使用串行回收器
- `-XX:+UseParNewGC`：新生代使用`ParNew`回收器，老年代使用串行回收器（`JDK9+`版本已删除该参数，因为`CMS`被`G1`代替）
- `-XX:+UseParallelGC`：新生代使用`ParallelGC`回收器，老年代使用串行回收器

# 4 并行回收器
并行回收期在串行回收器的基础上进行了改进，使用多个线程同时对垃圾进行回收，常见的并行回收器有：

- 新生代`ParNew`回收器
- 新生代`ParallelGC`回收器
- 老年代`ParallelOldGC`回收器

## 4.1 `ParNew`
`ParNew`是一个工作在新生代的垃圾回收器，只是简单地将串行回收器多线程化，回收策略、算法、参数和新生代串行回收器一样。同时，`ParNew`也是独占式的回收器，回收过程中会`STW`。虽然`ParNew`采用了多线程进行垃圾回收，但是在单`CPU`或者并发能力较弱的系统中，并行回收器的效果有可能还要比串行回收器差。

开启`ParNew`可以使用如下参数：

- `-XX:+UseParNewGC`：新生代使用`ParNew`，老年代使用串行回收器（`JDK9+`已删除）
- `-XX:+UseConcMarkSweepGC`：新生代使用`ParNew`，老年代使用`CMS`（`JDK9+`不建议，建议使用默认的`G1`）

`ParNew`工作时的线程数量可以使用`-XX:ParallelGCThreads`指定。

## 4.2 `ParallelGC`
`ParallelGC`是使用复制算法的回收器，与`ParNew`的相同点是，都是多线程、独占式的回收器，但是，`ParallelGC`会关注系统的吞吐量，可以通过如下参数启用`ParallelGC`：

- `-XX:+UseParallelGC`：新生代使用`ParallelGC`，老年代使用串行回收器
- `-XX:+UseParallelOldGC`：新生代使用`ParallelGC`，老年代使用`ParallelOldGC`

`ParallelGC`提供了两个参数控制系统的吞吐量：

- `-XX:+MaxGCPauseMills`：设置最大垃圾回收停顿时间，一个大于0的整数。`ParallelGC`在工作的时候会调整`Java`堆大小或者其他参数，尽可能把停顿时间控制在`MaxGCPauseMills`以内，如果希望把停顿时间设置得很小，那么可能会使用一个较小的堆，因为较小的堆回收速度快于较大的堆，但后果是可能会导致垃圾回收的次数增多，有可能会降低吞吐量
- `-XX:+GCTimeRatio`：设置吞吐量大小，是一个`0-100`的整数，假设为`n`，那么系统将花费不超过`1/(1+n)`的时间进行垃圾回收，默认值为`99`，也就是用于垃圾回收的时间不得超过`1/(1+99)=1%`

另外还有一个`-XX:+UseAdaptiveSizePolicy`的参数，可以开启自适应策略，开启后，新生代大小、`eden`区和`survivor`区比例、晋升老年代的对象年龄等参数都会被自动调整。

## 4.3 `ParallelOldGC`
从名字就可以知道这是一个工作在老年代的`ParallelGC`，一样关注系统吞吐量，使用了标记压缩法，`JDK 1.6+`可用。相关参数如下：

- `-XX:+UseParallelOldGC`：指定在老年代使用`ParallelOldGC`（同时新生代使用`ParallelGC`）
- `-XX:ParallelGCThreads`：设置垃圾回收时的线程数量

# 5 `CMS`
`CMS`是`Concurrent Mark Sweep`的缩写，可以翻译为并发标记清除，一个使用标记清除法的多线程回收器，不会回收新生代。`CMS`与`ParallelGC`/`ParallelOldGC`不同，`CMS`主要关注的是系统停顿时间。

## 5.1 工作流程
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210427131940440.png)

详细说明如下：

- 初始标记：`STW`，作用是标记存活的对象，内容包括老年代中的所有`GC Roots`（`Java`中的`GC Roots`包括虚拟机栈引用的对象、方法区中类静态属性引用的对象、方法区中的常量引用的对象、本地方法栈中`JNI`引用的对象），以及新生代中引用到老年代对象的对象
- 并发标记：从初始标记阶段标记的对象开始找出所有存活的对象
- 预清理：因为并发标记并不能标记出老年代全部的存活对象（标记的同时应用程序会改变一些对象的引用），这个阶段是用于处理并发标记阶段因为引用关系改变而导致没有标记到的存活对象的（可以使用`-XX:-CMSPrecleaningEnabled`关闭）
- 重新标记：`STW`，目标是完成标记整个老年代的所有存活对象。如果此阶段花费时间过长，可以使用`-XX:+CMSScavengeBeforeRemark`，在重新标记之前进行`Yong GC`，不过该参数有可能会导致频繁的`CMS GC`，原因可以[戳这里](https://cloud.tencent.com/developer/article/1413725)
- 并发清理：清除没有标记的对象并回收空间，当然由于这个过程是并发的，也就是用户线程也会运行，而此时产生的垃圾无法被清理，只能留到下一次`GC`再清理，这部分垃圾就称为“浮动垃圾”
- 并发重置：重新设置`CMS`内部的数据结果，准备下一次`CMS`使用

## 5.2 主要参数
- `-XX:+UseConcMarkSweepGC`：开启`CMS`
- `-XX:ConcGCThreads`/`-XX:ParallelCMSThreads`：设置并发线程数
- `-XX:CMSInitiatingOccupancyFraction`：回收阈值，当老年代使用率超过该值的时候就进行回收，默认为`68`，如果内存使用增长率过快，导致`CMS`执行过程中出现内存不足的情况，`CMS`就会回收失败，`JVM`会启动老年代串行回收器进行回收，同时会触发`STW`，直到回收完成
- `-XX:+UseCMSCompactAtFullCollection`：因为`CMS`是一个并发回收器，回收后很大可能会出现大量的内存碎片，导致离散的可用空间无法分配给大对象，并再次触发`CMS GC`。使用该参数后，会在回收完成后进行一次内存压缩（表现为整理内存碎片，非并发）
- `-XX:CMSFullGCsBeforeCompaction`：用于设定多少次`CMS`后，进行一次内存压缩

# 6 `G1`
`G1`是`JDK7`引入的垃圾回收器，在`JDK9+`作为默认回收器，特点包括：

- 并行性：回收期间可以由多个`GC`线程同时工作
- 并发性：部分工作可以和应用程序同时执行，一般不会在整个回收期阻塞应用程序
- 分代`GC`：兼顾新生代与老年代
- 空间整理：回收过程中会有适当的对象移动
- 可预见性：只选取部分区域进行内存回收，缩小了回收范围，同时可以控制`STW`时间

## 6.1 `G1`工作流程
`G1`的回收过程可能有4个阶段：

- 新生代`GC`
- 并发标记周期
- 混合回收
- （可选）`Full GC`

下面来分别看一下。

### 6.1.1 新生代`GC`
新生代`GC`的工作区域是`eden`区以及`survivor`区，一旦`eden`区占满，新生代`GC`就会启动。新生代`GC`后，所有的`eden`区会被清空，老年代的区域有可能增多（因为部分`survivor`区或`eden`区的对象晋升到老年代）。

比如下面是新生代`G1 GC`日志的一部分：
```bash
[1.076s][info][gc,start     ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
[1.076s][info][gc,task      ] GC(0) Using 2 workers of 10 for evacuation
[1.079s][info][gc,phases    ] GC(0)   Pre Evacuate Collection Set: 0.0ms
[1.079s][info][gc,phases    ] GC(0)   Evacuate Collection Set: 2.4ms
[1.079s][info][gc,phases    ] GC(0)   Post Evacuate Collection Set: 0.1ms
[1.079s][info][gc,phases    ] GC(0)   Other: 0.2ms
[1.079s][info][gc,heap      ] GC(0) Eden regions: 9->0(7)
[1.079s][info][gc,heap      ] GC(0) Survivor regions: 0->2(2)
[1.079s][info][gc,heap      ] GC(0) Old regions: 0->1
[1.079s][info][gc,heap      ] GC(0) Humongous regions: 0->0
[1.079s][info][gc,metaspace ] GC(0) Metaspace: 3473K->3473K(1056768K)
[1.079s][info][gc           ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 9M->2M(20M) 2.689ms
[1.079s][info][gc,cpu       ] GC(0) User=0.00s Sys=0.00s Real=0.01s
```
可以看到`eden`区域被清空，`survivor`区与老年区增多。

### 6.1.2 并发标记周期
`G1`的并发标记阶段和`CMS`有类似的地方，可以分为以下几步：

- 初始标记（`STW`）：标记从根节点直接可达的对象，这个阶段会伴随着一次新生代`GC`
- 根区域扫描（并发）：扫描由`survivor`区可直达的老年区域，并标记这些直接可达的对象
- 并发标记（并发）：和`CMS`类似，会扫描并查找整个堆的存活对象，并做好标记，这是一个并发的过程，但是会被新生代的`GC`打断
- 重新标记（`STW`）：对标记结果进行修正，使用`SATB`（`Snapshot-At-The-Beginning`）算法，在标记之初为存活对象创建一个快照，这个快照有助于加速重新标记的速度
- 独占清理（`STW`）：计算各个区域的存活对象和`GC`回收比例并进行排序，在这个阶段还会更新记忆集（`Remebered Set`）
- 并发清理（并发）：并发清理垃圾

其中比较重要的一个阶段是并发标记阶段，在并发标记后，会增加一些标记为`G`的区域，这些区域被标记为`G`是因为内部的垃圾比例高，希望在后续的`GC`中进行收集，而这些被标记为`G`的区域会被`G1`记录在一个称为`Collection Sets`的集合中。

### 6.1.3 混合回收
在并发标记周期中，虽然有部分对象被回收，但是总体上来说回收的比例是相当低的，但是在并发标记周期后，`G1`已经明确知道哪些区域有比较多的垃圾对象，在下一阶段就可以对其进行回收。

这个阶段就叫混合回收，因为既会执行正常的年轻代`GC`，也会选取一些被标记的老年代区域进行回收，同时处理了新生代和老年代。混合`GC`会执行多次，直到回收了足够的内存空间，然后它会触发一次新生代`GC`，然后不断循环，整体流程如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210427181933160.png)

### 6.1.4 `Full GC`
如果在并发回收的期间出现了内存不足，`G1`就会像`CMS`一样执行`Full GC`。另外，如果混合`GC`的时候空间不足，或者新生代`GC`时`survivor`区和老年代无法容纳幸存对象，都会导致一次`Full GC`。

## 6.2 `G1`相关参数
- `-XX:+UseG1GC`：启用`G1`
- `-XX:MaxGCPauseMills`：`STW`最大时间，如果任意一次停顿时间超过设置值，`G1`会尝试自动调整新生代、老年代的比例、调整堆大小等
- `-XX:ParallelGCThreads`：用于设置并行回收时`GC`的工作线程数
- `-XX:InitiatingHeapOccupancyPercent`：可以指定整个堆的使用率到达多少的时候，触发并发标记周期的执行，默认是`45`。一旦设置了该值，`G1`始终不会去修改，如果设置过大，意味着并发周期会迟迟得不到启动，引起`Full GC`的可能性会大大增加，如果设置得过小，并发周期会执行得非常频繁，大量`GC`线程抢占`CPU`导致性能下降


# 7 `GC`调优简单实验
## 7.1 概述
一个简单的实验，测试不同的`JVM`启动参数对`Tomcat`的影响，通过压力测试，获得`JVM`主要性能指标，体验不同参数对系统性能的影响。环境：

- `Tomcat 10.0.5`
- `OpenJDK 11.0.10`
- `JMeter 5.4.1`

## 7.2 步骤
### 7.2.1 添加线程组
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210427190736669.png)

`Test Plan`中选择右键，`Thread(Users)`，再选择`Thread Group`，设置线程数以及循环次数：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210428012848289.png)

### 7.2.2 添加采样器
选中刚才添加的线程组，并选择界面中的`Edit->Add->Sampler->HTTP Request`，添加`HTTP`采样器：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210427201708660.png)

这里选择了默认的`Tomcat`页面进行测试，端口`1080`。

### 7.2.3 添加总结报告
选中`HTTP Request`后，右键选择`Add->Listener->Summary Request`，添加总结报告，完成后就可以进行测试了。

## 7.3 测试
先引入环境变量：
```bash
export CATALINA_OPTS="-Xlog:gc:gc.log -Xmx32m -Xms32m -XX:ParallelGCThreads=4" 
```
接下来的操作都以该环境变量为主，首先设置初始堆和最大堆为`32m`，设置好后运行`Tomcat`，并在`JMeter`中进行测试，下面是`GC`日志的前100行：
```bash
[0.040s][info][gc] Using G1
[0.377s][info][gc] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 14M->3M(32M) 2.699ms
[0.573s][info][gc] GC(1) Pause Young (Normal) (G1 Evacuation Pause) 14M->5M(32M) 2.605ms
[0.678s][info][gc] GC(2) Pause Young (Normal) (G1 Evacuation Pause) 16M->6M(32M) 2.355ms
[0.793s][info][gc] GC(3) Pause Young (Normal) (G1 Evacuation Pause) 17M->7M(32M) 1.579ms
[0.796s][info][gc] GC(4) Pause Young (Concurrent Start) (Metadata GC Threshold) 7M->7M(32M) 0.925ms
[0.796s][info][gc] GC(5) Concurrent Cycle
[0.808s][info][gc] GC(5) Pause Remark 8M->8M(32M) 2.363ms
[0.815s][info][gc] GC(5) Pause Cleanup 9M->9M(32M) 0.021ms
[0.816s][info][gc] GC(5) Concurrent Cycle 19.666ms
[0.899s][info][gc] GC(6) Pause Young (Normal) (G1 Evacuation Pause) 19M->8M(32M) 1.150ms
[1.018s][info][gc] GC(7) Pause Young (Normal) (G1 Evacuation Pause) 20M->9M(32M) 1.243ms
[17.760s][info][gc] GC(8) Pause Young (Normal) (G1 Evacuation Pause) 22M->15M(32M) 2.984ms
[17.810s][info][gc] GC(9) Pause Young (Normal) (G1 Evacuation Pause) 22M->19M(32M) 2.921ms
[17.818s][info][gc] GC(10) Pause Young (Concurrent Start) (G1 Evacuation Pause) 22M->21M(32M) 1.168ms
[17.818s][info][gc] GC(11) Concurrent Cycle
[17.822s][info][gc] GC(12) Pause Young (Normal) (G1 Evacuation Pause) 23M->23M(32M) 1.129ms
[17.830s][info][gc] GC(13) Pause Young (Normal) (G1 Evacuation Pause) 24M->24M(32M) 1.426ms
[17.836s][info][gc] GC(14) Pause Young (Normal) (G1 Evacuation Pause) 25M->25M(32M) 1.050ms
[17.843s][info][gc] GC(15) Pause Young (Normal) (G1 Evacuation Pause) 26M->26M(32M) 1.195ms
[17.853s][info][gc] GC(11) Pause Remark 27M->27M(32M) 3.820ms
[17.855s][info][gc] GC(16) Pause Young (Normal) (G1 Evacuation Pause) 27M->26M(32M) 1.672ms
[17.858s][info][gc] GC(17) Pause Young (Normal) (G1 Evacuation Pause) 27M->26M(32M) 1.069ms
[17.869s][info][gc] GC(18) Pause Young (Normal) (G1 Evacuation Pause) 27M->27M(32M) 1.121ms
[17.872s][info][gc] GC(19) Pause Young (Normal) (G1 Evacuation Pause) 28M->27M(32M) 0.811ms
[17.876s][info][gc] GC(20) Pause Young (Normal) (G1 Evacuation Pause) 28M->28M(32M) 0.867ms
[17.878s][info][gc] GC(11) Pause Cleanup 29M->29M(32M) 0.029ms
[17.879s][info][gc] GC(21) Pause Young (Prepare Mixed) (G1 Evacuation Pause) 29M->28M(32M) 0.905ms
[17.879s][info][gc] GC(11) Concurrent Cycle 60.929ms
[17.885s][info][gc] GC(22) To-space exhausted
[17.885s][info][gc] GC(22) Pause Young (Mixed) (G1 Evacuation Pause) 29M->30M(32M) 2.788ms
[17.891s][info][gc] GC(23) To-space exhausted
[17.891s][info][gc] GC(23) Pause Young (Concurrent Start) (G1 Evacuation Pause) 31M->31M(32M) 2.017ms
[17.891s][info][gc] GC(25) Concurrent Cycle
[17.915s][info][gc] GC(24) Pause Full (G1 Evacuation Pause) 31M->24M(32M) 24.037ms
[17.915s][info][gc] GC(25) Concurrent Cycle 24.201ms
[17.918s][info][gc] GC(26) Pause Young (Normal) (G1 Evacuation Pause) 25M->25M(32M) 0.881ms
[17.921s][info][gc] GC(27) Pause Young (Concurrent Start) (G1 Evacuation Pause) 26M->25M(32M) 1.092ms
[17.921s][info][gc] GC(28) Concurrent Cycle
[17.924s][info][gc] GC(29) Pause Young (Normal) (G1 Evacuation Pause) 26M->25M(32M) 0.842ms
[17.931s][info][gc] GC(30) Pause Young (Normal) (G1 Evacuation Pause) 26M->26M(32M) 2.357ms
[17.933s][info][gc] GC(31) Pause Young (Normal) (G1 Evacuation Pause) 27M->26M(32M) 1.058ms
[17.936s][info][gc] GC(32) Pause Young (Normal) (G1 Evacuation Pause) 27M->26M(32M) 0.966ms
[17.941s][info][gc] GC(33) Pause Young (Normal) (G1 Evacuation Pause) 27M->27M(32M) 0.911ms
[17.954s][info][gc] GC(34) Pause Young (Normal) (G1 Evacuation Pause) 28M->27M(32M) 1.532ms
[17.961s][info][gc] GC(35) To-space exhausted
[17.961s][info][gc] GC(35) Pause Young (Normal) (G1 Evacuation Pause) 28M->29M(32M) 1.326ms
[17.967s][info][gc] GC(36) To-space exhausted
[17.967s][info][gc] GC(36) Pause Young (Normal) (G1 Evacuation Pause) 30M->30M(32M) 1.425ms
[17.989s][info][gc] GC(37) Pause Full (G1 Evacuation Pause) 30M->28M(32M) 22.554ms
[17.989s][info][gc] GC(28) Concurrent Cycle 68.160ms
[17.993s][info][gc] GC(38) Pause Young (Normal) (G1 Evacuation Pause) 29M->29M(32M) 0.951ms
[17.997s][info][gc] GC(39) To-space exhausted
[17.997s][info][gc] GC(39) Pause Young (Concurrent Start) (G1 Evacuation Pause) 30M->30M(32M) 1.763ms
[17.997s][info][gc] GC(41) Concurrent Cycle
[18.020s][info][gc] GC(40) Pause Full (G1 Evacuation Pause) 30M->29M(32M) 22.459ms
[18.020s][info][gc] GC(41) Concurrent Cycle 22.538ms
[18.028s][info][gc] GC(42) To-space exhausted
[18.028s][info][gc] GC(42) Pause Young (Normal) (G1 Evacuation Pause) 30M->30M(32M) 1.399ms
[18.049s][info][gc] GC(43) Pause Full (G1 Evacuation Pause) 30M->30M(32M) 21.067ms
[18.058s][info][gc] GC(44) To-space exhausted
[18.058s][info][gc] GC(44) Pause Young (Concurrent Start) (G1 Evacuation Pause) 31M->31M(32M) 1.830ms
[18.058s][info][gc] GC(46) Concurrent Cycle
[18.080s][info][gc] GC(45) Pause Full (G1 Evacuation Pause) 31M->30M(32M) 22.113ms
[18.080s][info][gc] GC(46) Concurrent Cycle 22.213ms
[18.169s][info][gc] GC(47) To-space exhausted
[18.169s][info][gc] GC(47) Pause Young (Normal) (G1 Evacuation Pause) 31M->31M(32M) 87.776ms
[18.192s][info][gc] GC(48) Pause Full (G1 Evacuation Pause) 31M->30M(32M) 22.622ms
[18.214s][info][gc] GC(49) Pause Full (G1 Evacuation Pause) 30M->30M(32M) 22.480ms
[18.216s][info][gc] GC(50) Pause Young (Concurrent Start) (G1 Evacuation Pause) 30M->30M(32M) 1.112ms
[18.216s][info][gc] GC(52) Concurrent Cycle
[18.241s][info][gc] GC(51) Pause Full (G1 Evacuation Pause) 30M->30M(32M) 25.469ms
[18.266s][info][gc] GC(53) Pause Full (G1 Evacuation Pause) 30M->30M(32M) 24.480ms
[18.266s][info][gc] GC(52) Concurrent Cycle 50.062ms
[18.267s][info][gc] GC(54) Pause Young (Normal) (G1 Evacuation Pause) 30M->30M(32M) 0.681ms
[18.293s][info][gc] GC(55) Pause Full (G1 Evacuation Pause) 30M->30M(32M) 25.581ms
[18.316s][info][gc] GC(56) Pause Full (G1 Evacuation Pause) 30M->30M(32M) 22.917ms
[18.317s][info][gc] GC(57) Pause Young (Concurrent Start) (G1 Evacuation Pause) 30M->30M(32M) 1.170ms
[18.317s][info][gc] GC(59) Concurrent Cycle
[18.342s][info][gc] GC(58) Pause Full (G1 Evacuation Pause) 30M->30M(32M) 24.189ms
[18.365s][info][gc] GC(60) Pause Full (G1 Evacuation Pause) 30M->30M(32M) 23.685ms
[18.365s][info][gc] GC(59) Concurrent Cycle 48.004ms
[18.366s][info][gc] GC(61) Pause Young (Normal) (G1 Evacuation Pause) 30M->30M(32M) 0.810ms
[18.393s][info][gc] GC(62) Pause Full (G1 Evacuation Pause) 30M->30M(32M) 26.309ms
[18.419s][info][gc] GC(63) Pause Full (G1 Evacuation Pause) 30M->30M(32M) 26.395ms
[18.421s][info][gc] GC(64) Pause Young (Concurrent Start) (G1 Evacuation Pause) 30M->30M(32M) 0.978ms
[18.421s][info][gc] GC(66) Concurrent Cycle
[18.447s][info][gc] GC(65) Pause Full (G1 Evacuation Pause) 30M->30M(32M) 26.732ms
[18.473s][info][gc] GC(67) Pause Full (G1 Evacuation Pause) 30M->30M(32M) 25.213ms
[18.473s][info][gc] GC(66) Concurrent Cycle 52.098ms
[18.474s][info][gc] GC(68) Pause Young (Normal) (G1 Evacuation Pause) 30M->30M(32M) 1.288ms
[18.503s][info][gc] GC(69) Pause Full (G1 Evacuation Pause) 30M->30M(32M) 28.438ms
[18.526s][info][gc] GC(70) Pause Full (G1 Evacuation Pause) 30M->30M(32M) 22.862ms
[18.527s][info][gc] GC(71) Pause Young (Concurrent Start) (G1 Evacuation Pause) 30M->30M(32M) 1.047ms
[18.527s][info][gc] GC(73) Concurrent Cycle
[18.551s][info][gc] GC(72) Pause Full (G1 Evacuation Pause) 30M->30M(32M) 24.183ms
[18.572s][info][gc] GC(74) Pause Full (G1 Evacuation Pause) 30M->30M(32M) 21.006ms
[18.573s][info][gc] GC(73) Concurrent Cycle 45.322ms
[18.574s][info][gc] GC(75) Pause Young (Normal) (G1 Evacuation Pause) 30M->30M(32M) 0.711ms
[18.598s][info][gc] GC(76) Pause Full (G1 Evacuation Pause) 30M->30M(32M) 24.588ms
```
可以看到频繁发生了`Full GC`。

## 7.4 调优
解决频繁发生`Full GC`的最简单一个方法就是将堆内存调大，使用如下参数再次启动`Tomcat`：
```bash
export CATALINA_OPTS="-Xlog:gc:gc.log -Xmx256m -Xms32m -XX:ParallelGCThreads=4"
```
日志如下：
```bash
[0.024s][info][gc] Using G1
[0.278s][info][gc] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 14M->3M(32M) 2.545ms
[0.355s][info][gc] GC(1) Pause Young (Normal) (G1 Evacuation Pause) 7M->4M(32M) 2.359ms
[0.485s][info][gc] GC(2) Pause Young (Normal) (G1 Evacuation Pause) 13M->5M(32M) 1.345ms
[0.595s][info][gc] GC(3) Pause Young (Normal) (G1 Evacuation Pause) 15M->6M(32M) 2.102ms
[0.686s][info][gc] GC(4) Pause Young (Concurrent Start) (Metadata GC Threshold) 16M->7M(32M) 3.140ms
[0.686s][info][gc] GC(5) Concurrent Cycle
[0.696s][info][gc] GC(5) Pause Remark 8M->8M(32M) 2.647ms
[0.700s][info][gc] GC(5) Pause Cleanup 8M->8M(32M) 0.019ms
[0.700s][info][gc] GC(5) Concurrent Cycle 13.683ms
[0.761s][info][gc] GC(6) Pause Young (Normal) (G1 Evacuation Pause) 17M->8M(32M) 1.689ms
[0.835s][info][gc] GC(7) Pause Young (Normal) (G1 Evacuation Pause) 19M->8M(32M) 1.680ms
[11.813s][info][gc] GC(8) Pause Young (Normal) (G1 Evacuation Pause) 19M->11M(32M) 2.670ms
[11.890s][info][gc] GC(9) Pause Young (Normal) (G1 Evacuation Pause) 21M->17M(32M) 4.077ms
[11.907s][info][gc] GC(10) Pause Young (Concurrent Start) (G1 Evacuation Pause) 22M->21M(32M) 1.528ms
[11.907s][info][gc] GC(11) Concurrent Cycle
[11.917s][info][gc] GC(12) Pause Young (Normal) (G1 Evacuation Pause) 23M->23M(32M) 1.918ms
[11.921s][info][gc] GC(13) Pause Young (Normal) (G1 Evacuation Pause) 24M->24M(32M) 0.955ms
[11.926s][info][gc] GC(14) Pause Young (Normal) (G1 Evacuation Pause) 25M->24M(32M) 0.733ms
[11.930s][info][gc] GC(15) Pause Young (Normal) (G1 Evacuation Pause) 25M->25M(32M) 0.769ms
[11.934s][info][gc] GC(11) Pause Remark 25M->25M(32M) 3.490ms
[11.937s][info][gc] GC(16) Pause Young (Normal) (G1 Evacuation Pause) 26M->25M(32M) 0.787ms
[11.945s][info][gc] GC(17) Pause Young (Normal) (G1 Evacuation Pause) 26M->25M(32M) 0.893ms
[11.949s][info][gc] GC(18) Pause Young (Normal) (G1 Evacuation Pause) 26M->26M(32M) 0.911ms
[11.949s][info][gc] GC(11) Pause Cleanup 26M->26M(32M) 0.029ms
[11.950s][info][gc] GC(11) Concurrent Cycle 42.921ms
[11.962s][info][gc] GC(19) Pause Young (Normal) (G1 Evacuation Pause) 27M->26M(32M) 0.855ms
[11.971s][info][gc] GC(20) Pause Young (Concurrent Start) (G1 Evacuation Pause) 27M->27M(32M) 1.335ms
[11.971s][info][gc] GC(21) Concurrent Cycle
[11.978s][info][gc] GC(22) Pause Young (Normal) (G1 Evacuation Pause) 28M->28M(32M) 0.853ms
[11.981s][info][gc] GC(23) Pause Young (Normal) (G1 Evacuation Pause) 29M->28M(32M) 0.777ms
[11.984s][info][gc] GC(24) Pause Young (Normal) (G1 Evacuation Pause) 29M->29M(64M) 0.944ms
[12.007s][info][gc] GC(21) Pause Remark 34M->34M(64M) 3.139ms
[12.032s][info][gc] GC(21) Pause Cleanup 39M->39M(64M) 0.041ms
[12.036s][info][gc] GC(25) Pause Young (Normal) (G1 Evacuation Pause) 39M->32M(64M) 3.190ms
[12.037s][info][gc] GC(21) Concurrent Cycle 65.196ms
[12.096s][info][gc] GC(26) Pause Young (Normal) (G1 Evacuation Pause) 41M->34M(64M) 2.597ms
[12.150s][info][gc] GC(27) Pause Young (Concurrent Start) (G1 Evacuation Pause) 43M->37M(64M) 2.926ms
[12.150s][info][gc] GC(28) Concurrent Cycle
[12.246s][info][gc] GC(28) Pause Remark 42M->42M(64M) 73.769ms
[12.259s][info][gc] GC(29) Pause Young (Normal) (G1 Evacuation Pause) 45M->38M(109M) 2.864ms
[12.263s][info][gc] GC(28) Pause Cleanup 40M->40M(109M) 0.037ms
[12.267s][info][gc] GC(28) Concurrent Cycle 117.019ms
[12.341s][info][gc] GC(30) Pause Young (Normal) (G1 Evacuation Pause) 59M->40M(109M) 3.691ms
[12.468s][info][gc] GC(31) Pause Young (Normal) (G1 Evacuation Pause) 72M->44M(109M) 3.743ms
[12.594s][info][gc] GC(32) Pause Young (Normal) (G1 Evacuation Pause) 76M->47M(109M) 3.134ms
[12.764s][info][gc] GC(33) Pause Young (Normal) (G1 Evacuation Pause) 79M->48M(109M) 2.044ms
[12.855s][info][gc] GC(34) Pause Young (Normal) (G1 Evacuation Pause) 80M->48M(109M) 2.071ms
[12.949s][info][gc] GC(35) Pause Young (Normal) (G1 Evacuation Pause) 82M->48M(109M) 1.615ms
[13.035s][info][gc] GC(36) Pause Young (Normal) (G1 Evacuation Pause) 83M->48M(109M) 1.681ms
[13.133s][info][gc] GC(37) Pause Young (Normal) (G1 Evacuation Pause) 83M->50M(109M) 3.947ms
[13.214s][info][gc] GC(38) Pause Young (Normal) (G1 Evacuation Pause) 85M->50M(109M) 3.206ms
[13.285s][info][gc] GC(39) Pause Young (Normal) (G1 Evacuation Pause) 85M->50M(109M) 2.007ms
[13.362s][info][gc] GC(40) Pause Young (Normal) (G1 Evacuation Pause) 87M->50M(109M) 2.705ms
[13.454s][info][gc] GC(41) Pause Young (Normal) (G1 Evacuation Pause) 90M->50M(109M) 3.772ms
```
吞吐量为`1.4w`每秒：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210427202729311.png)

将堆大小调大后，可以明显看到`GC`次数减少，且没有发生`Full GC`，此时的可以将并发量增加，观察性能瓶颈，比如将线程数调到`2000`，循环数不变：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210427203011744.png)

再次测试，日志如下（最后50行）：
```bash
[7.554s][info][gc] GC(73) Pause Young (Concurrent Start) (G1 Evacuation Pause) 114M->82M(132M) 1.920ms
[7.554s][info][gc] GC(74) Concurrent Cycle
[7.590s][info][gc] GC(74) Pause Remark 99M->99M(132M) 4.054ms
[7.620s][info][gc] GC(74) Pause Cleanup 113M->113M(132M) 0.089ms
[7.620s][info][gc] GC(74) Concurrent Cycle 66.091ms
[7.624s][info][gc] GC(75) Pause Young (Normal) (G1 Evacuation Pause) 114M->82M(132M) 2.885ms
[7.677s][info][gc] GC(76) Pause Young (Concurrent Start) (G1 Evacuation Pause) 114M->82M(132M) 2.369ms
[7.677s][info][gc] GC(77) Concurrent Cycle
[7.730s][info][gc] GC(78) Pause Young (Normal) (G1 Evacuation Pause) 114M->82M(132M) 2.615ms
[7.756s][info][gc] GC(77) Pause Remark 95M->95M(132M) 2.964ms
[7.793s][info][gc] GC(79) Pause Young (Normal) (G1 Evacuation Pause) 114M->82M(132M) 5.707ms
[7.811s][info][gc] GC(77) Pause Cleanup 92M->92M(132M) 0.255ms
[7.812s][info][gc] GC(77) Concurrent Cycle 134.823ms
[7.854s][info][gc] GC(80) Pause Young (Normal) (G1 Evacuation Pause) 114M->82M(132M) 2.604ms
[7.912s][info][gc] GC(81) Pause Young (Concurrent Start) (G1 Evacuation Pause) 114M->82M(132M) 1.952ms
[7.912s][info][gc] GC(82) Concurrent Cycle
[7.940s][info][gc] GC(82) Pause Remark 94M->94M(132M) 3.422ms
[7.960s][info][gc] GC(82) Pause Cleanup 105M->105M(132M) 0.061ms
[7.960s][info][gc] GC(82) Concurrent Cycle 47.595ms
[7.976s][info][gc] GC(83) Pause Young (Prepare Mixed) (G1 Evacuation Pause) 114M->81M(132M) 2.423ms
[7.985s][info][gc] GC(84) Pause Young (Mixed) (G1 Evacuation Pause) 86M->81M(132M) 1.495ms
[8.038s][info][gc] GC(85) Pause Young (Concurrent Start) (G1 Evacuation Pause) 113M->81M(132M) 2.309ms
[8.038s][info][gc] GC(86) Concurrent Cycle
[8.079s][info][gc] GC(86) Pause Remark 104M->104M(132M) 3.507ms
[8.098s][info][gc] GC(87) Pause Young (Normal) (G1 Evacuation Pause) 114M->81M(132M) 3.336ms
[8.106s][info][gc] GC(86) Pause Cleanup 86M->86M(132M) 0.112ms
[8.106s][info][gc] GC(86) Concurrent Cycle 67.767ms
[8.148s][info][gc] GC(88) Pause Young (Normal) (G1 Evacuation Pause) 114M->81M(132M) 2.621ms
[8.205s][info][gc] GC(89) Pause Young (Concurrent Start) (G1 Evacuation Pause) 114M->81M(132M) 2.943ms
[8.205s][info][gc] GC(90) Concurrent Cycle
[8.263s][info][gc] GC(91) Pause Young (Normal) (G1 Evacuation Pause) 114M->81M(132M) 2.117ms
[8.274s][info][gc] GC(90) Pause Remark 84M->84M(132M) 4.372ms
[8.309s][info][gc] GC(90) Pause Cleanup 102M->102M(132M) 0.082ms
[8.309s][info][gc] GC(90) Concurrent Cycle 103.562ms
[8.331s][info][gc] GC(92) Pause Young (Prepare Mixed) (G1 Evacuation Pause) 114M->81M(132M) 2.712ms
[8.342s][info][gc] GC(93) Pause Young (Mixed) (G1 Evacuation Pause) 86M->80M(132M) 1.982ms
[8.392s][info][gc] GC(94) Pause Young (Normal) (G1 Evacuation Pause) 114M->80M(132M) 1.921ms
[8.437s][info][gc] GC(95) Pause Young (Normal) (G1 Evacuation Pause) 114M->80M(132M) 1.980ms
[8.487s][info][gc] GC(96) Pause Young (Normal) (G1 Evacuation Pause) 114M->80M(132M) 1.965ms
[8.528s][info][gc] GC(97) Pause Young (Normal) (G1 Evacuation Pause) 114M->80M(132M) 1.959ms
[8.600s][info][gc] GC(98) Pause Young (Normal) (G1 Evacuation Pause) 114M->80M(132M) 5.305ms
[8.655s][info][gc] GC(99) Pause Young (Normal) (G1 Evacuation Pause) 115M->80M(132M) 2.709ms
[8.709s][info][gc] GC(100) Pause Young (Normal) (G1 Evacuation Pause) 115M->80M(132M) 1.762ms
[8.759s][info][gc] GC(101) Pause Young (Normal) (G1 Evacuation Pause) 115M->80M(132M) 1.767ms
[8.801s][info][gc] GC(102) Pause Young (Normal) (G1 Evacuation Pause) 115M->80M(132M) 1.739ms
[8.850s][info][gc] GC(103) Pause Young (Normal) (G1 Evacuation Pause) 115M->80M(132M) 1.696ms
[8.899s][info][gc] GC(104) Pause Young (Normal) (G1 Evacuation Pause) 115M->80M(132M) 1.499ms
[8.952s][info][gc] GC(105) Pause Young (Normal) (G1 Evacuation Pause) 115M->80M(132M) 1.289ms
[8.999s][info][gc] GC(106) Pause Young (Normal) (G1 Evacuation Pause) 115M->80M(132M) 1.219ms
[9.043s][info][gc] GC(107) Pause Young (Normal) (G1 Evacuation Pause) 115M->80M(132M) 1.110ms
```
吞吐量为`2.3w`每秒：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210427203322511.png)

相同的参数下，将线程数增加，吞吐量增加了，说明还没到达性能瓶颈，再次增大并发线程数：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210427203541236.png)

日志如下：
```bash
[58.313s][info][gc] GC(354) Pause Young (Normal) (G1 Evacuation Pause) 217M->209M(241M) 3.415ms
[58.328s][info][gc] GC(355) Pause Young (Normal) (G1 Evacuation Pause) 220M->210M(241M) 1.408ms
[58.354s][info][gc] GC(356) Pause Young (Normal) (G1 Evacuation Pause) 220M->210M(241M) 4.860ms
[58.378s][info][gc] GC(353) Pause Remark 221M->221M(241M) 5.735ms
[58.392s][info][gc] GC(357) Pause Young (Normal) (G1 Evacuation Pause) 221M->210M(241M) 1.799ms
[58.407s][info][gc] GC(353) Pause Cleanup 218M->218M(241M) 0.430ms
[58.408s][info][gc] GC(353) Concurrent Cycle 109.426ms
[58.416s][info][gc] GC(358) Pause Young (Prepare Mixed) (G1 Evacuation Pause) 221M->210M(241M) 1.584ms
[58.431s][info][gc] GC(359) Pause Young (Mixed) (G1 Evacuation Pause) 221M->187M(241M) 1.880ms
[58.463s][info][gc] GC(360) Pause Young (Mixed) (G1 Evacuation Pause) 197M->165M(244M) 2.684ms
[58.485s][info][gc] GC(361) Pause Young (Mixed) (G1 Evacuation Pause) 175M->144M(244M) 4.659ms
[58.505s][info][gc] GC(362) Pause Young (Mixed) (G1 Evacuation Pause) 154M->124M(244M) 5.943ms
[58.522s][info][gc] GC(363) Pause Young (Mixed) (G1 Evacuation Pause) 134M->118M(244M) 3.665ms
[58.640s][info][gc] GC(364) Pause Young (Normal) (G1 Evacuation Pause) 163M->119M(247M) 3.835ms
[58.722s][info][gc] GC(365) Pause Young (Normal) (G1 Evacuation Pause) 170M->119M(247M) 1.531ms
[58.823s][info][gc] GC(366) Pause Young (Normal) (G1 Evacuation Pause) 178M->119M(247M) 1.982ms
[58.926s][info][gc] GC(367) Pause Young (Normal) (G1 Evacuation Pause) 185M->120M(247M) 2.277ms
[59.023s][info][gc] GC(368) Pause Young (Normal) (G1 Evacuation Pause) 191M->120M(247M) 3.918ms
[59.192s][info][gc] GC(369) Pause Young (Normal) (G1 Evacuation Pause) 194M->120M(247M) 2.634ms
[59.346s][info][gc] GC(370) Pause Young (Normal) (G1 Evacuation Pause) 205M->120M(247M) 2.053ms
[59.479s][info][gc] GC(371) Pause Young (Normal) (G1 Evacuation Pause) 206M->120M(247M) 2.384ms
[59.615s][info][gc] GC(372) Pause Young (Normal) (G1 Evacuation Pause) 207M->120M(247M) 3.700ms
[59.733s][info][gc] GC(373) Pause Young (Normal) (G1 Evacuation Pause) 207M->120M(247M) 6.038ms
[59.917s][info][gc] GC(374) Pause Young (Normal) (G1 Evacuation Pause) 208M->120M(247M) 2.311ms
[60.062s][info][gc] GC(375) Pause Young (Normal) (G1 Evacuation Pause) 209M->120M(247M) 2.319ms
[60.197s][info][gc] GC(376) Pause Young (Normal) (G1 Evacuation Pause) 210M->120M(247M) 2.315ms
[60.316s][info][gc] GC(377) Pause Young (Normal) (G1 Evacuation Pause) 210M->120M(247M) 3.419ms
[60.456s][info][gc] GC(378) Pause Young (Normal) (G1 Evacuation Pause) 212M->120M(247M) 2.019ms
[60.638s][info][gc] GC(379) Pause Young (Normal) (G1 Evacuation Pause) 212M->120M(247M) 2.782ms
[60.799s][info][gc] GC(380) Pause Young (Normal) (G1 Evacuation Pause) 212M->120M(247M) 2.341ms
[60.947s][info][gc] GC(381) Pause Young (Normal) (G1 Evacuation Pause) 213M->120M(247M) 2.954ms
[61.102s][info][gc] GC(382) Pause Young (Normal) (G1 Evacuation Pause) 217M->120M(247M) 2.598ms
[61.234s][info][gc] GC(383) Pause Young (Concurrent Start) (G1 Evacuation Pause) 216M->120M(247M) 2.340ms
[61.234s][info][gc] GC(384) Concurrent Cycle
[61.271s][info][gc] GC(384) Pause Remark 133M->133M(247M) 4.457ms
[61.287s][info][gc] GC(384) Pause Cleanup 135M->135M(247M) 0.171ms
[61.288s][info][gc] GC(384) Concurrent Cycle 53.972ms
[61.444s][info][gc] GC(385) Pause Young (Prepare Mixed) (G1 Evacuation Pause) 216M->120M(247M) 2.913ms
[61.464s][info][gc] GC(386) Pause Young (Mixed) (G1 Evacuation Pause) 131M->103M(247M) 3.910ms
[61.486s][info][gc] GC(387) Pause Young (Mixed) (G1 Evacuation Pause) 114M->95M(247M) 3.828ms
[61.684s][info][gc] GC(388) Pause Young (Normal) (G1 Evacuation Pause) 200M->95M(247M) 2.013ms
[61.881s][info][gc] GC(389) Pause Young (Normal) (G1 Evacuation Pause) 215M->95M(247M) 2.089ms
[62.073s][info][gc] GC(390) Pause Young (Concurrent Start) (G1 Evacuation Pause) 217M->95M(247M) 2.686ms
[62.073s][info][gc] GC(391) Concurrent Cycle
[62.103s][info][gc] GC(391) Pause Remark 106M->106M(247M) 3.136ms
[62.122s][info][gc] GC(391) Pause Cleanup 118M->118M(247M) 0.111ms
[62.123s][info][gc] GC(391) Concurrent Cycle 49.728ms
[62.334s][info][gc] GC(392) Pause Young (Prepare Mixed) (G1 Evacuation Pause) 217M->95M(247M) 2.472ms
[62.348s][info][gc] GC(393) Pause Young (Mixed) (G1 Evacuation Pause) 106M->75M(247M) 1.981ms
[62.363s][info][gc] GC(394) Pause Young (Mixed) (G1 Evacuation Pause) 86M->59M(247M) 3.422ms
```
吞吐量`2.7w`：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210427203729120.png)

由于篇幅限制，其他方法就不再叙述了，如果想再提高吞吐量，可以从下面几个方面入手：

- 调大堆内存：`-Xmx1g`
- 使用更多的线程：`-XX:ParallelGCThreads=8`
- 设置更大的初始堆内存：`-Xms512m`
- 设置更大的新生代：`-XX:G1NewSizePercent`+`-XX:G1MaxNewSizePercent`

# 8 附录一：回收的一些细节讨论
## 8.1 禁用显式`GC`
一般情况下，`System.gc()`会触发`Full GC`，同时对老年代和新生代进行回收，`JVM`提供了一个`DisableExplicitGC`来控制是否可以显式触发`GC`。`System.gc()`底层是`native`方法，源码位于`jvm.cpp`中：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210427145330225.png)

如果禁用了，就相当于是空实现，也就是什么也不会执行。

## 8.2 显式`GC`使用并发回收
默认情况下，如果`System.gc()`生效，会使用传统的`Full GC`，同时会忽略参数中的`UseG1GC`以及`UseConcMarkSweepGC`，此时`CMS`/`G1`都是没有并发执行的，如果使用`-XX:+ExplicitGCInvokesConcurrent`后，就会改变这种默认行为。

比如下面的代码：
```java
public static void main(String[] args){
    byte [] b = new byte[1024*1024*10];
    System.gc();
}
```
带上参数：
```bash
-Xlog:gc*,gc+marking*=debug,gc+heap=debug
-Xmx30m
```
会触发`Full GC`：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210427204914920.png)

而如果加上`-XX:+ExplicitGCInvokesConcurrent`后，不会发生`Full GC`，而是使用`G1`的并行`GC`：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210427205100371.png)

## 8.3 关于对象如何晋升到老年代
对象晋升为老年代的途径有以下几个：

- 通过年龄晋升：在`survivor`区中存活到一定年龄后（默认是15），便进入老年代，但是需要注意对象的实际晋升年龄是由`survivor`的使用情况动态计算得来的，也就是说，默认情况下，年龄到达15一定晋升到老年代，但是未到达该年龄的对象也有可能晋升，可以通过`-XX:MaxTenuringThresold`设置晋升年龄、
- 通过大小晋升：如果对象很大，大到`eden`区和`survivor`区都无法容纳，则会直接晋升到老年代，可以通过`-XX:PreteureSizeThreshold`设置，单位为字节

## 8.4 关于`TLAB`
`TLAB`全称是`Thread Local Allocation Buffer`，线程本地缓存分配，这是一个线程专用的内存分配区域。使用该区域的原因是为了加速对象的分配，尽管对象一般分配在堆上，而堆是所有线程共享的，同一时间可能会有多个线程申请堆空间，容易造成冲突，而对象分配是一种非常常见的操作，因此`Java`提供了`TLAB`来避免分配对象时的线程冲突，提高对象分配的效率。在`TLAB`启用的情况下，虚拟机会为每一个`Java`线程分配一块`TLAB`区域。

### 8.4.1 一个简单的测试
测试代码：
```java
public static void main(String[] args){
    long start = System.nanoTime();
    for (int i = 0; i < 1_0000_0000; i++) {
        byte [] b = new byte[2];
        b[0] = 1;
    }
    long end = System.nanoTime();
    System.out.println(end-start);
}
```
参数：
```bash
-server -XX:+UseTLAB -Xcomp -XX:-BackgroundCompilation -XX:+DoEscapeAnalysis
```
输出：
```bash
1013561
```
修改参数，关闭`TLAB`：
```bash
-server -XX:-UseTLAB -Xcomp -XX:-BackgroundCompilation -XX:+DoEscapeAnalysis
```
输出：
```bash
3154586
```
可以看到，开启了`TLAB`花费的时间大概是没有开启`TLAB`的时间的三分之一。

### 8.4.2 对象的分配
从上面的实验可以看到，`TLAB`对对象分配的影响还是很大的，但是，由于`TLAB`的空间通常比较小，很容易装满，比如`TLAB`为`100KB`，已经使用了`80KB`，如果需要分配一个`30KB`的对象，那么可以有两种处理办法：

- 放弃当前的`TLAB`区域：就是重新再申请一块`TLAB`，但是这样会浪费原来`TLAB`剩下的`20KB`
- 直接分配在堆上：保留当前的`TLAB`，将来如果有小于`20KB`的对象就可以直接使用剩下的`20KB`

因此，`JVM`内部会维护一个叫`refill_waste`的值：

- 当请求的对象大于`refill_waste`时，会选择在堆分配
- 若小于该值，废弃当前`TLAB`，新建`TLAB`来分配新对象

默认情况下，`TLAB`和`refill_waste`的大小都会在运行时不断调整，使系统的运行状态最优。

引入`TLAB`后，对象的分配流程如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/2021042721230183.png)

# 9 附录二： 常用`GC`参数总结
## 9.1 串行回收器相关参数
- `-XX:+UseSerialGC`：新生代和老年代使用串行回收器
- `-XX:SurvivorRatio`：设置`eden`区和`survivor`区大小比例
- `-XX:PretenureSizeThreshold`：设置大对象进入老年代的阈值，超过该值会被直接分配在老年代
- `-XX:MaxTenuringThreshold`：设置对象进入老年代的最大值，每一次`Minor GC`后对象年龄就会加1，大于这个年龄的对象会进入老年代

## 9.2 并行回收器相关参数
- `-XX:+UseParNewGC`：新生代使用并行回收器，老年代使用串行回收器（`JDK9+`已删除）
- `-XX:+UseParallelOldGC`：老年代使用`ParallelOldGC`，新生代使用`ParallelGC`
- `-XX:+ParallelGCThreads`：设置用于垃圾回收的线程数
- `-XX:MaxGCPauseMills`：最大垃圾回收停顿时间，一个大于0的整数
- `-XX:GCTimeRatio`：设置吞吐量大小，一个`0-100`的整数
- `-XX:+UseAdaptiveSizePolicy`：打开自适应策略，新生代的大小、`eden`区和`survivor`区比例、晋升到老年代的对象年龄参数会被动态调整

## 9.3 `CMS`相关参数
- `-XX:+UseConcMarkSweepGC`：新生代使用并行回收器，老年代使用`CMS`+串行回收器
- `-XX:ParallelCMSThreads`：设定`CMS`的线程数量
- `-XX:CMSInitiatingOccupancyFraction`：设置垃圾回收在老年代空间被使用多少后触发，默认为使用率为`68%`
- `-XX:+UseCMSCompactAtFullCollection`：设置垃圾回收后是否需要进行一次内存碎片整理
- `-XX:CMSFullGCsBeforeCompaction`：设定进行多少次`CMS`后，进行一次内存压缩
- `-XX:+CMSClassUnloadingEnabled`：允许对类元数据区进行回收
- `-XX:CMSInitiatingPermOccupancyFraction`：当永久区占用率达到该百分比后，进行一次`CMS GC`，前提开启`-XX:+CMSClassUnloadingEnabled`
- `-XX:+CMSIncrementalMode`：使用增量模式（`JDK9`移除）

## 9.4 `G1`相关参数
- `-XX:+UseG1GC`：开启`G1`
- `-XX:MaxGCPauseMills`：设置最大垃圾回收停顿时间
- `-XX:GCPauseIntervalMills`：设置停顿时间间隔

## 9.5 `TLAB`相关参数
- `-XX:+UseTLAB`：开启`TLAB`
- `-XX:+PrintTLAB`：打印相关信息（`JDK9`不支持）
- `-XX:TLABSize`：设置`TLAB`区域大小
- `-XX:+ResizeTLAB`：自动调整`TLAB`大小

## 9.6 其他参数
- `-XX:+DisableExplicitGC`：禁用显式`GC`
- `-XX:+ExplicitGCInvokesConcurrent`：使用并发方式处理显式`GC`

# 10 参考

- [掘金-JVM原理之GC垃圾回收器CMS详解](https://juejin.cn/post/6844903970142421005)
- [Understanding Java Garbage Collection Logging: What Are GC Logs and How To Analyze Them](https://sematext.com/blog/java-garbage-collection-logs/)
