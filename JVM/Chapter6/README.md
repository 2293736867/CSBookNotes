# Table of Contents

* [1 来源](#1-来源)
* [2 概述](#2-概述)
* [3 对象头](#3-对象头)
* [4 锁的运行时优化](#4-锁的运行时优化)
  * [4.1 偏向锁（`JDK15`默认关闭）](#41-偏向锁jdk15默认关闭)
    * [4.1.1 简介](#411-简介)
    * [4.1.2 加锁流程](#412-加锁流程)
    * [4.1.3 例子](#413-例子)
  * [4.2 轻量级锁](#42-轻量级锁)
    * [4.2.1 简介](#421-简介)
    * [4.2.2 加锁流程](#422-加锁流程)
  * [4.3 重量级锁](#43-重量级锁)
    * [4.3.1 简介](#431-简介)
    * [4.3.2 加锁流程](#432-加锁流程)
  * [4.4 自旋锁](#44-自旋锁)
  * [4.5 锁消除](#45-锁消除)
    * [4.5.1 简介](#451-简介)
    * [4.5.2 例子](#452-例子)
* [5 锁的应用层优化](#5-锁的应用层优化)
  * [5.1 减少持有时间](#51-减少持有时间)
  * [5.2 减小粒度](#52-减小粒度)
  * [5.3 锁分离](#53-锁分离)
  * [5.4 锁粗化](#54-锁粗化)
* [6 无锁：`CAS`](#6-无锁cas)
* [7 参考](#7-参考)


# 1 来源
- 来源：《Java虚拟机 JVM故障诊断与性能优化》——葛一鸣
- 章节：第八章

本文是第八章的一些笔记整理。

# 2 概述
本文主要讲述了`JVM`在运行层面和代码层面的锁优化策略，最后介绍了实现无锁的其中一种方法`CAS`。

# 3 对象头
`JVM`中每个对象都有一个对象头，用于保存对象的系统信息，`64bit JVM`的对象头结构如下图所示：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210429104615245.png)

其中：

- `Mark Word`由`64bit`组成，一个功能数据区，可以存放对象的哈希、对象年龄、锁的指针等信息
- `KClass Word`在没有开启指针压缩的情况下，`64bit`组成，但是`64bit JVM`会默认开启指针压缩（`+UseCompressedOops`），所以会压缩到`32bit`

另外，从图中可以看到，不同的锁对应于不同的`Mark Word`：

- 无锁：`25bit`空+`31bit`哈希值+`1bit`空+`4bit`分代年龄+`1bit`是否偏向锁+`2bit`锁标记
- 偏向锁：`54bit`持有偏向锁的线程`ID`+`2bit`偏向时间戳+`1bit`空+`4bit`分代年龄+`1bit`是否偏向锁+`2bit`锁标记
- 轻量锁：`62bit`栈中锁记录指针+`2bit`锁标记
- 重量锁：`62bit`重量级锁指针+`2bit`锁标记

`JVM`如何区分锁主要看两个字段：`biased_lock`与`lock`，对应关系如下：

- `biased_lock=0 lock=00`：轻量级锁
- `biased_lock=0 lock=01`：无锁
- `biased_lock=0 lock=10`：重量级锁
- `biased_lock=0 lock=11`：`GC`标记
- `biased_lock=1 lock=01`：偏向锁

# 4 锁的运行时优化
很多时候`JVM`都会对线程竞争的操作在`JVM`层面进行优化，尽可能解决竞争问题，也会试图消除不必要的竞争，实现的方法包括：

- 偏向锁
- 轻量级锁
- 重量级锁
- 自旋锁
- 锁消除

## 4.1 偏向锁（`JDK15`默认关闭）
### 4.1.1 简介
偏向锁是`JDK 1.6`提出的一种锁优化方式，核心思想是，如果线程没有竞争，则取消已经取得锁的线程同步操作，也就是说，某个线程获取到锁后，锁就会进入偏向模式，当线程再次请求该锁时，无需再次进行相关的同步操作，从而节省操作时间。而在此期间如果有其他线程进行了锁请求，则锁退出偏向模式。

开启偏向锁的参数是`-XX:+UseBiasedLocking`，处于偏向锁时，`Mark Word`会记录获得锁的线程（`54bit`），通过该信息可以判断当前线程是否持有偏向锁。

注意`JDK15`后默认关闭了偏向锁以及禁用了相关选项，可以参考[JDK-8231264](https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8231264)。

### 4.1.2 加锁流程
偏向锁的加锁过程如下：

- 第一步：访问`Mark Word`中的`biased_lock`是否设置为`1`，`lock`是否设置为`01`，确认为可偏向状态，如果`biased_lock`为`0`，则是无锁状态，直接通过`CAS`操作竞争锁，如果失败，执行第四步
- 第二步：如果为可偏向状态，测试线程`ID`是否指向当前线程，如果是，到达第五步，否则到达第三步
- 第三步：如果线程`ID`没有指向当前线程，通过`CAS`操作竞争锁，如果成功，将`Mark Word`中的线程`ID`设置为当前线程`ID`，然后执行第五步，如果失败，执行第四步
- 第四步：如果`CAS`获取偏向锁失败，表示有竞争，开始锁撤销
- 第五步：执行同步代码

### 4.1.3 例子
下面是一个简单的例子：
```java
public class Main {
    private static List<Integer> list = new Vector<>();
    public static void main(String[] args){
        long start = System.nanoTime();
        for (int i = 0; i < 1_0000_0000; i++) {
            list.add(i);
        }
        long end = System.nanoTime();
        System.out.println(end-start);
    }
}
```
`Vector`的`add`是一个`synchronized`方法，使用如下参数测试：
```bash
-XX:BiasedLockingStartupDelay=0 # 偏向锁启动时间，设置为0表示立即启动
-XX:+UseBiasedLocking # 开启偏向锁
```
输出如下：
```bash
1664109780
```
而将偏向锁关闭：
```bash
-XX:BiasedLockingStartupDelay=0
-XX:-UseBiasedLocking
```
输出如下：
```bash
2505048191
```
可以看到偏向锁还是对系统性能有一定帮助的，但是需要注意偏向锁在锁竞争激烈的场合没有太强的优化效果，因为大量的竞争会导致持有锁的线程不停地切换，锁很难一直保持在偏向模式，这样不仅仅不能优化性能，反而因为频繁切换而导致性能下降，因此竞争激烈的场合可以尝试使用`-XX:-UseBiasedLocking`禁用偏向锁。

## 4.2 轻量级锁
### 4.2.1 简介
如果偏向锁失败，那么`JVM`会让线程申请轻量级锁。轻量级锁在内部使用一个`BasicObjectLock`的对象实现，该对象内部由：

- 一个`BasicLock`对象
- 一个持有该锁的`Java`对象指针

组成。`BasicObjectLock`对象放置在`Java`栈的栈帧中，在`BasicLock`对象还会维护一个叫`displaced_header`的字段，用于备份对象头部的`Mark Word`。

### 4.2.2 加锁流程
- 第一步：通过`Mark Word`判断是否无锁（`biased_lock`是否为`0`且`lock`为`01`），如果是无锁，会创建一个叫锁记录（`Lock Record`）的空间，用于存储当前`Mark Word`的拷贝
- 第二步：将对象头的`Mark Word`复制到锁记录中
- 第三步：拷贝成功后，使用`CAS`操作尝试将锁对象`Mark Word`更新为指向锁记录的指针，并将线程栈帧中的锁记录的`owner`指向`Object`的`Mark Word`
- 第四步：如果操作成功，那么就成功拥有了锁
- 第五步：如果操作失败，`JVM`会检查`Mark Word`是否指向当前线程的栈帧，如果是就说明当前线程已经拥有了这个对象的锁，就可以直接进入同步块继续执行，否则会让当前线程尝试自旋获取锁，自旋到达一定次数后如果还没有获得锁，那么会膨胀为重量级锁

## 4.3 重量级锁
### 4.3.1 简介
当轻量级锁自旋一定次数后还是无法获取锁，就会膨胀为重量级锁。相比起轻量级锁，`Mak Word`存放的是指向锁记录的指针，重量级锁中的`Mark Word`存放的是指向`Object Monitor`的指针，如下图所示：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210430103445676.png)

（图源见文末）

因为锁记录是线程私有的，不能满足多线程都能访问的需求，因此重量级锁中引入了能线程共享的`ObjectMonitor`。

### 4.3.2 加锁流程
初次尝试加锁时，会先`CAS`尝试修改`ObjectMonitor`的`_owner`字段，结果如下：

- 第一种：锁没有其他线程占用，成功获取锁
- 第二种：锁被其他线程占用，则当前线程重入锁，获取成功
- 第三种：锁被锁记录占用，而锁记录是线程私有的，也就是属于当前线程的，这样就属于重入，重入次数为1
- 第四种：都不满足，再次尝试加锁（调用`EnterI()`）

而再次尝试加锁的过程，是一个循环，不断尝试获取锁直到成功为止，流程简述如下：

- 多次尝试获取锁
- 获取失败把线程包装后放进阻塞队列
- 再次尝试获取锁
- 失败后将自己挂起
- 被唤醒后继续尝试获取锁
- 成功则退出循环，否则继续

## 4.4 自旋锁
自旋锁可以使线程没有取得锁时不被挂起，而是去执行一个空循环（也就是所谓的自旋），在若干个空循环后如果可以获取锁，则继续执行，如果不能，挂起当前线程。

使用自旋锁后，线程被挂起的概率相对减小，线程执行的连贯性相对加强，因此对于锁竞争不是很激烈、锁占用并发时间很短的并发线程具有一定的积极意义，但是，对于竞争激烈且锁占用时间长的并发线程，自旋等待后仍无法获取锁，还是会被挂起，浪费了自旋时间。

在`JDK1.6`中提供了`-XX:+UseSpinning`参数开启自旋锁，但是`JDK1.7`后，自旋锁参数被取消，`JVM`不再支持由用户配置自旋锁，自旋锁总是被执行，次数由`JVM`调整。

## 4.5 锁消除
### 4.5.1 简介
锁消除就是把不必要的锁给去掉，比如，在一些单线程环境下使用一些线程安全的类，比如`StringBuffer`，这样就可以基于逃逸分析技术可消除这些不必要的锁，从而提高性能。

### 4.5.2 例子
```java
public class Main {
    private static final int CIRCLE = 200_0000;
    public static void main(String[] args){
        long start = System.nanoTime();
        for (int i = 0; i < CIRCLE; i++) {
            createStringBuffer("Test",String.valueOf(i));
        }
        long end = System.nanoTime();
        System.out.println(end-start);
    }

    private static String createStringBuffer(String s1,String s2){
        StringBuffer sb = new StringBuffer();
        sb.append(s1);
        sb.append(s2);
        return sb.toString();
    }
}
```
参数：
```bash
-XX:+DoEscapeAnalysis
-XX:-EliminateLocks
-Xcomp
-XX:-BackgroundCompilation
-XX:BiasedLockingStartupDelay=0
```
输出：
```bash
260642198
```
而开启锁消除后：
```bash
-XX:+DoEscapeAnalysis
-XX:+EliminateLocks
-Xcomp
-XX:-BackgroundCompilation
-XX:BiasedLockingStartupDelay=0
```
输出如下：
```bash
253101105
```
可以看到还是有一定性能提升的，但是提升不大。

# 5 锁的应用层优化
锁的应用层优化就是在代码层面对锁进行优化，方法包括：

- 减少持有时间
- 减小粒度
- 锁分离
- 锁粗化

## 5.1 减少持有时间
减少锁持有时间就是尽可能减少某个锁的占用时间，以减少线程互斥时间，比如：
```java
public synchronized void method(){
	A();
	B();
	C();
}
```
如果只有`B()`是同步操作，那么可以优化为在必要时进行同步，也就是在执行`B()`的时候进行同步操作：
```java
public void method(){
	A();
	synchronized(this){
		B();
	}
	C();
}
```

## 5.2 减小粒度
所谓的减小锁粒度，就是指缩小锁定的对象范围，从而减小锁冲突的可能性，进而提高系统的并发能力。

减小粒度也是一种削弱多线程竞争的有效手段，比如典型的就是`ConcurrentHashMap`，在`JDK1.7`中的`segment`就是一个很好的例子。每次并发操作的时候只加锁某个特定的`segment`，从而提高并发性能。

## 5.3 锁分离
锁分离就是将一个独占锁分成多个锁，比如`LinkedBlockingQueue`。在`take()`和`put()`操作中，使用的并不是同一个锁，而是分离成了一个`takeLock`和一个`putLock`：
```java
private final ReentrantLock takeLock;
private final ReentrantLock putLock;
```
初始化操作如下：
```java
this.takeLock = new ReentrantLock();
this.notEmpty = this.takeLock.newCondition();
this.putLock = new ReentrantLock();
```
而`take()`和`put()`操作如下：
```java
public E take() throws InterruptedException {
    takeLock.lockInterruptibly();  //不能两个线程同时take
    //...
    try {
        //...
    } finally {
        takeLock.unlock();
    }
    //...
}

public void put(E e) throws InterruptedException {
	//...
    putLock.lockInterruptibly();  //不能两个线程同时put
    try {
        //...
    } finally {
        putLock.unlock();
    }
	//...
}
```
可以看到通过`putLock`以及`takeLock`两把锁实现了真正的取数据与写数据分离


## 5.4 锁粗化
通常情况下，为了保证多线程的有效并发，会要求每个线程持有锁的时间尽可能短，但是，如果对同一个锁不停请求，本身也会消耗资源，反而不利于性能优化，于是，在遇到一连串连续对同一个锁不断进行请求和释放的操作时，会把所有的锁操作整合成对锁的一次请求，减少对锁的请求同步次数，这个过程就叫锁粗化，比如
```java
public void method(){
	synchronized(lock){
		A();
	}
	synchronized(lock){
		B();
	}
}
```
会被整合成如下形式：
```java
public void method(){
	synchronized(lock){
		A();
		B();
	}
}
```
而在循环内申请锁，比如：
```java
for(int i=0;i<10;++i){
	synchronized(lock){
	}
}
```
应将锁粗化为
```java
synchronized(lock){
	for(int i=0;i<10;++i){
	}
}
```

# 6 无锁：`CAS`
毫无疑问，为了保证多线程并发的安全，使用锁是一种最直观的方式，但是，锁的竞争有可能会称为瓶颈，因此，有没有不需要锁的方式去保证数据一致性呢？

答案是有的，就是这一小节介绍的主角：`CAS`。

`CAS`就是`Compare And Swap`的缩写，`CAS`包含三个参数，形式为`CAS(V,E,N)`，其中：

- `V`表示内存地址值
- `E`表示期望值
- `N`表示新值

只有当`V`的值等于`E`的值时，才会把`V`设置为`N`，如果`V`的值和`N`的值不一样，那么表示已经有其他线程做了更新，当前线程什么也不做，最后`CAS`返回当前`V`的值。

`CAS`的操作是抱着乐观的态度进行的，总认为自己可以成功完成操作，当多个线程同时使用`CAS`操作同一个变量的时候，只会有一个胜出并成功更新，其他均会失败。失败的线程不会被挂起，仅被告知失败，并且允许再次尝试，当然也允许失败的线程放弃操作。

# 7 参考
- [CSDN-java对象头信息](https://blog.csdn.net/zhaocuit/article/details/100208879)
- [JVM系列之:详解java object对象在heap中的结构](https://cloud.tencent.com/developer/article/1667980)
- [StackOverflow-What is in Java object header?](https://stackoverflow.com/questions/26357186/what-is-in-java-object-header)
- [CSDN-Java 中锁是如何一步步膨胀的（偏向锁、轻量级锁、重量级锁）](https://blog.csdn.net/weixin_44584387/article/details/104763837)
- [简书-Java Synchronized 重量级锁原理深入剖析上(互斥篇)](https://www.jianshu.com/p/8a8d2b42ddca)
