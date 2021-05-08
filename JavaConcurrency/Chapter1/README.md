# Table of Contents

* [1 来源](#1-来源)
* [2 概述](#2-概述)
* [3 线程生命周期](#3-线程生命周期)
  * [3.1 五个阶段](#31-五个阶段)
  * [3.2 `NEW`](#32-new)
  * [3.3 `RUNNABLE`](#33-runnable)
  * [3.4 `RUNNING`](#34-running)
  * [3.5 `BLOCKED`](#35-blocked)
  * [3.6 `TERMINATED`](#36-terminated)
* [4 `Thread`构造方法](#4-thread构造方法)
  * [4.1 构造方法](#41-构造方法)
  * [4.2 线程的父子关系](#42-线程的父子关系)
  * [4.3 关于`stackSize`](#43-关于stacksize)
* [5 `Thread API`](#5-thread-api)
  * [5.1 `sleep()`](#51-sleep)
  * [5.2 `yield()`](#52-yield)
  * [5.3 `setPriority()`](#53-setpriority)
    * [5.3.1 优先级介绍](#531-优先级介绍)
    * [5.3.2 优先级源码分析](#532-优先级源码分析)
  * [5.4 `interrupt()`](#54-interrupt)
    * [5.4.1 `interrupt()`](#541-interrupt)
    * [5.4.2 `isInterrupted()`](#542-isinterrupted)
    * [5.4.3 `interrupted()`](#543-interrupted)
  * [5.5 `join()`](#55-join)
    * [5.5.1 `join()`简介](#551-join简介)
    * [5.5.2 例子](#552-例子)
* [6 线程关闭](#6-线程关闭)
  * [6.1 正常关闭](#61-正常关闭)
    * [6.1.1 正常结束](#611-正常结束)
    * [6.1.2 捕获信号关闭线程](#612-捕获信号关闭线程)
    * [6.1.3 `volatile`](#613-volatile)
  * [6.2 异常退出](#62-异常退出)
  * [6.3 假死](#63-假死)


# 1 来源

- 来源：《Java高并发编程详解 多线程与架构设计》，汪文君著
- 章节：第一、二、三章

本文是前三章的笔记整理。

# 2 概述
本文主要讲述了线程的生命周期、`Thread`类的构造方法以及常用`API`，最后介绍了线程的关闭方法。

# 3 线程生命周期
## 3.1 五个阶段
线程生命周期可以分为五个阶段：

- `NEW`
- `RUNNABLE`
- `RUNNING`
- `BLOCKED`
- `TERMINATED`

## 3.2 `NEW`
用`new`创建一个`Thread`对象时，但是并没有使用`start()`启动线程，此时线程处于`NEW`状态。准确地说，只是`Thread`对象的状态，这就是一个普通的`Java`对象。此时可以通过`start()`方法进入`RUNNABLE`状态。

## 3.3 `RUNNABLE`
进入`RUNNABLE`状态必须调用`start()`方法，这样就在`JVM`中创建了一个线程。但是，线程一经创建，并不能马上被执行，线程执行与否需要听令于`CPU`调度，也就是说，此时是处于可执行状态，具备执行的资格，但是并没有真正执行起来，而是在等待被调度。

`RUNNABLE`状态只能意外终止或进入`RUNNING`状态。

## 3.4 `RUNNING`
一旦`CPU`通过轮询或其他方式从任务可执行队列中选中了线程，此时线程才能被执行，也就是处于`RUNNING`状态，在该状态中，可能发生的状态转换如下：

- 进入`TERMINATED`：比如调用已经不推荐的`stop()`方法
- 进入`BLOCKED`：比如调用了`sleep()`/`wait()`方法，或者进行某个阻塞操作（获取锁资源、磁盘`IO`等）
- 进入`RUNNABLE`：`CPU`时间片到，或者线程主动调用`yield()`

## 3.5 `BLOCKED`
也就是阻塞状态，进入阻塞状态的原因很多，常见的如下：

- 磁盘`IO`
- 网络操作
- 为了获取锁而进入阻塞操作

处于`BLOCKED`状态时，可能发生的状态转换如下：

- 进入`TERMINATED`：比如调用不推荐的`stop()`，或者`JVM`意外死亡
- 进入`RUNNABLE`：比如休眠结束、被`notify()`/`nofityAll()`唤醒、获取到某个锁、阻塞过程被`interrupt()`打断等

## 3.6 `TERMINATED`
`TERMINATED`是线程的最终状态，进入该状态后，意味着线程的生命周期结束，比如在下列情况下会进入该状态：

- 线程运行正常结束
- 线程运行出错意外结束
- `JVM`意外崩溃，导致所有线程都强制结束

# 4 `Thread`构造方法
## 4.1 构造方法
`Thread`的构造方法一共有八个，这里根据命名方式分类，使用默认命名的构造方法如下：

- `Thread()`
- `Thread(Runnable target)`
- `Thread(ThreadGroup group,Runnable target)`

命名线程的构造方法如下：

- `Thread(String name)`
- `Thread(Runnable target,Strintg name)`
- `Thread(ThreadGroup group,String name)`
- `Thread(ThreadGroup group,Runnable target,String name)`
- `Thread(ThreadGroup group,Runnable target,String name,long stackSize)`

但实际上所有的构造方法最终都是调用如下私有构造方法：
```java
private Thread(ThreadGroup g, Runnable target, String name, long stackSize, AccessControlContext acc, boolean inheritThreadLocals);
```

在默认命名构造方法中，在源码中可以看到，默认命名其实就是`Thread-X`的命令（X为数字）：
```java
public Thread() {
    this((ThreadGroup)null, (Runnable)null, "Thread-" + nextThreadNum(), 0L);
}

public Thread(Runnable target) {
    this((ThreadGroup)null, target, "Thread-" + nextThreadNum(), 0L);
}

private static synchronized int nextThreadNum() {
    return threadInitNumber++;
}
```
而在命名构造方法就是自定义的名字。

另外，如果想修改线程的名字，可以调用`setName()`方法，但是需要注意，处于`NEW`状态的线程才能修改。

## 4.2 线程的父子关系
`Thread`的所有构造方法都会调用如下方法：
```java
private Thread(ThreadGroup g, Runnable target, String name, long stackSize, AccessControlContext acc, boolean inheritThreadLocals);
```
其中的一段源码截取如下：
```java
if (name == null) {
    throw new NullPointerException("name cannot be null");
} else {
    this.name = name;
    Thread parent = currentThread();
    SecurityManager security = System.getSecurityManager();
    if (g == null) {
        if (security != null) {
            g = security.getThreadGroup();
        }

        if (g == null) {
            g = parent.getThreadGroup();
        }
    }
}
```
可以看到当前这里有一个局部变量叫`parent`，并且赋值为`currentThread()`，`currentThread()`是一个`native`方法。因为一个线程被创建时的最初状态为`NEW`，因此`currentThread()`代表是创建自身线程的那个线程，也就是说，结论如下：

- 一个线程的创建肯定是由另一个线程完成的
- 被创建线程的父线程是创建它的线程

也就是自己创建的线程，父线程为`main`线程，而`main`线程由`JVM`创建。

另外，`Thread`的构造方法中有几个具有`ThreadGroup`参数，该参数指定了线程位于哪一个`ThreadGroup`，如果一个线程创建的时候没有指定`ThreadGroup`，那么将会和父线程同一个`ThreadGroup`。`main`线程所在的`ThreadGroup`称为`main`。

## 4.3 关于`stackSize`
`Thread`构造方法中有一个`stackSize`参数，该参数指定了`JVM`分配线程栈的地址空间的字节数，对平台依赖性较高，在一些平台上：

- 设置较大的值：可以使得线程内调用递归深度增加，降低`StackOverflowError`出现的概率
- 设置较低的值：可以使得创建的线程数增多，可以推迟`OutOfMemoryError`出现的时间

但是，在一些平台上该参数不会起任何作用。另外，如果设置为0也不会起到任何作用。

# 5 `Thread API`
## 5.1 `sleep()`
`sleep()`有两个重载方法：

- `sleep(long mills)`
- `sleep(long mills,int nanos)`

但是在`JDK1.5`后，引入了`TimeUnit`，其中对`sleep()`方法提供了很好的封装，建议使用`TimeUnit.XXXX.sleep()`去代替`Thread.sleep()`：
```java
TimeUnit.SECONDS.sleep(1);
TimeUnit.MINUTES.sleep(3);
```

## 5.2 `yield()`
`yield()`属于一种启发式方法，提醒`CPU`调度器当前线程会自愿放弃资源，如果`CPU`资源不紧张，会忽略这种提醒。调用`yield()`方法会使当前线程从`RUNNING`变为`RUNNABLE`状态。

关于`yield()`与`sleep()`的区别，区别如下：

- `sleep()`会导致当前线程暂停指定的时间，没有`CPU`时间片的消耗
- `yield()`只是对`CPU`调度器的一个提示，如果`CPU`调度器没有忽略这个提示，会导致线程上下文的切换
- `sleep()`会使线程短暂阻塞，在给定时间内释放`CPU`资源
- 如果`yield()`生效，`yield()`会使得从`RUNNING`状态进入`RUNNABLE`状态
- `sleep()`会几乎百分百地完成给定时间的休眠，但是`yield()`的提示不一定能担保
- 一个线程调用`sleep()`而另一个线程调用`interrupt()`会捕获到中断信号，而`yield`则不会

## 5.3 `setPriority()`
### 5.3.1 优先级介绍
线程与进程类似，也有自己的优先级，理论上来说，优先级越高的线程会有优先被调度的机会，但实际上并不是如此，设置优先级与`yield()`类似，也是一个提醒性质的操作：

- 对于`root`用户，会提醒操作系统想要设置的优先级别，否则会被忽略
- 如果`CPU`比较忙，设置优先级可能会获得更多的`CPU`时间片，但是空闲时优先级的高低几乎不会有任何作用

所以，设置优先级只是很大程度上让某个线程尽可能获得比较多的执行机会，也就是让线程自己尽可能被操作系统调度，而不是设置了高优先级就一定优先运行，或者说优先级高的线程比优先级低的线程就一定优先运行。

### 5.3.2 优先级源码分析
设置优先级直接调用`setPriority()`即可，`OpenJDK 11`源码如下：
```java
public final void setPriority(int newPriority) {
    this.checkAccess();
    if (newPriority <= 10 && newPriority >= 1) {
        ThreadGroup g;
        if ((g = this.getThreadGroup()) != null) {
            if (newPriority > g.getMaxPriority()) {
                newPriority = g.getMaxPriority();
            }

            this.setPriority0(this.priority = newPriority);
        }

    } else {
        throw new IllegalArgumentException();
    }
}
```
可以看到优先级处于`[1,10]`之间，而且不能设置为大于当前`ThreadGroup`的优先级，最后通过`native`方法`setPriority0`设置优先级。

一般情况下，不会对线程的优先级设置级别，默认情况下，线程的优先级为5，因为`main`线程的优先级为5，而且`main`为所有线程的父进程，因此默认情况下线程的优先级也是5。

## 5.4 `interrupt()`
`interrupt()`是一个重要的`API`，线程中断的`API`有如下三个：

- `void interrupt()`
- `boolean isInterrupted()`
- `static boolean interrupted()`

下面对其逐一进行分析。

### 5.4.1 `interrupt()`
一些方法调用会使得当前线程进入阻塞状态，比如：

- `Object.wait()`
- `Thread.sleep()`
- `Thread.join()`
- `Selector.wakeup()`

而调用`interrupt()`可以打断阻塞，打断阻塞并不等于线程的生命周期结束，仅仅是打断了当前线程的阻塞状态。一旦在阻塞状态下被打断，就会抛出一个`InterruptedException`的异常，这个异常就像一个信号一样通知当前线程被打断了，例子如下：
```java
public static void main(String[] args) throws InterruptedException{
    Thread thread = new Thread(()->{
        try{
            TimeUnit.SECONDS.sleep(10);
        }catch (InterruptedException e){
            System.out.println("Thread is interrupted.");
        }
    });
    thread.start();
    TimeUnit.SECONDS.sleep(1);
    thread.interrupt();
}
```
会输出线程被中断的信息。

### 5.4.2 `isInterrupted()`
`isInterrupted()`可以判断当前线程是否被中断，仅仅是对`interrupt()`标识的一个判断，并不会影响标识发生任何改变（因为调用`interrupt()`的时候会设置内部的一个叫`interrupt flag`的标识），例子如下：
```java
public static void main(String[] args) throws InterruptedException{
    Thread thread = new Thread(()->{
        while (true){}
    });
    thread.start();
    TimeUnit.SECONDS.sleep(1);
    System.out.println("Thread is interrupted :"+thread.isInterrupted());
    thread.interrupt();
    System.out.println("Thread is interrupted :"+thread.isInterrupted());
}
```
输出结果为：
```bash
Thread is interrupted :false
Thread is interrupted :true
```
另一个例子如下：
```java
public static void main(String[] args) throws InterruptedException {
    Thread thread = new Thread() {
        @Override
        public void run() {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    System.out.println("In catch block thread is interrupted :" + isInterrupted());
                }
            }
        }
    };
    thread.start();
    TimeUnit.SECONDS.sleep(1);
    System.out.println("Thread is interrupted :" + thread.isInterrupted());
    thread.interrupt();
    TimeUnit.SECONDS.sleep(1);
    System.out.println("Thread is interrupted :" + thread.isInterrupted());
}
```
输出结果：
```bash
Thread is interrupted :false
In catch block thread is interrupted :false
Thread is interrupted :false
```
一开始线程未被中断，结果为`false`，调用中断方法后，在循环体内捕获到了异常（信号），此时会`Thread`自身会擦除`interrupt`标识，将标识复位，因此捕获到异常后输出结果也为`false`。

### 5.4.3 `interrupted()`
这是一个静态方法，调用该方法会擦除掉线程的`interrupt`标识，需要注意的是如果当前线程被打断了：

- 第一次调用`interrupted()`会返回`true`，并且立即擦除掉`interrupt`标识
- 第二次包括以后的调用永远都会返回`false`，除非在此期间线程又一次被打断

例子如下：
```java
public static void main(String[] args) throws InterruptedException {
    Thread thread = new Thread() {
        @Override
        public void run() {
            while (true) {
                System.out.println(Thread.interrupted());
            }
        }
    };
    thread.setDaemon(true);
    thread.start();
    TimeUnit.MILLISECONDS.sleep(2);
    thread.interrupt();
}
```
输出（截取一部分）：
```bash
false
false
false
true
false
false
false
```
可以看到其中带有一个`true`，也就是`interrupted()`判断到了其被中断，此时会立即擦除中断标识，并且只有该次返回`true`，后面都是`false`。

关于`interrupted()`与`isInterrupted()`的区别，可以从源码（`OpenJDK 11`）知道：
```java
public static boolean interrupted() {
    return currentThread().isInterrupted(true);
}

public boolean isInterrupted() {
    return this.isInterrupted(false);
}

@HotSpotIntrinsicCandidate
private native boolean isInterrupted(boolean var1);
```
实际上两者都是调用同一个`native`方法，其中的布尔变量表示是否擦除线程的`interrupt`标识：

- `true`表示想要擦除，`interrupted()`就是这样做的
- `false`表示不想擦除，`isInterrupted()`就是这样做的

## 5.5 `join()`
### 5.5.1 `join()`简介
`join()`与`sleep()`一样，都是属于可以中断的方法，如果其他线程执行了对当前线程的`interrupt`操作，也会捕获到中断信号，并且擦除线程的`interrupt`标识，`join()`提供了三个`API`，分别如下：

- `void join()`
- `void join(long millis,int nanos)`
- `void join(long mills)`

### 5.5.2 例子
一个简单的例子如下：
```java
public class Main {
    public static void main(String[] args) throws InterruptedException {
        List<Thread> threads = IntStream.range(1,3).mapToObj(Main::create).collect(Collectors.toList());
        threads.forEach(Thread::start);
        for (Thread thread:threads){
            thread.join();
        }
        for (int i = 0; i < 10; i++) {
            System.out.println(Thread.currentThread().getName()+" # "+i);
            shortSleep();
        }
    }

    private static Thread create(int seq){
        return new Thread(()->{
            for (int i = 0; i < 10; i++) {
                System.out.println(Thread.currentThread().getName()+" # "+i);
                shortSleep();
            }
        },String.valueOf(seq));
    }

    private static void shortSleep(){
        try{
            TimeUnit.MILLISECONDS.sleep(2);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
```
输出截取如下：
```cpp
2 # 8
1 # 8
2 # 9
1 # 9
main # 0
main # 1
main # 2
main # 3
main # 4
```
线程1和线程2交替执行，而`main`线程会等到线程1和线程2执行完毕后再执行。

# 6 线程关闭
`Thread`中有一个过时的方法`stop`，可以用于关闭线程，但是存在的问题是有可能不会释放`monitor`的锁，因此不建议使用该方法关闭线程。线程的关闭可以分为三类：

- 正常关闭
- 异常退出
- 假死

## 6.1 正常关闭
### 6.1.1 正常结束
线程运行结束后，就会正常退出，这是最普通的一种情况。
### 6.1.2 捕获信号关闭线程
通过捕获中断信号去关闭线程，例子如下：
```java
public static void main(String[] args) throws InterruptedException {
    Thread t = new Thread(){
        @Override
        public void run() {
            System.out.println("work...");
            while(!isInterrupted()){

            }
            System.out.println("exit...");
        }
    };
    t.start();
    TimeUnit.SECONDS.sleep(5);
    System.out.println("System will be shutdown.");
    t.interrupt();
}
```
一直检查`interrupt`标识是否设置为`true`，设置为`true`则跳出循环。另一种方式是使用`sleep()`：
```java
public static void main(String[] args) throws InterruptedException {
    Thread t = new Thread(){
        @Override
        public void run() {
            System.out.println("work...");
            while(true){
                try{
                    TimeUnit.MILLISECONDS.sleep(1);
                }catch (InterruptedException e){
                    break;
                }
            }
            System.out.println("exit...");
        }
    };
    t.start();
    TimeUnit.SECONDS.sleep(5);
    System.out.println("System will be shutdown.");
    t.interrupt();
}
```

### 6.1.3 `volatile`
由于`interrupt`标识很有可能被擦除，或者不会调用`interrupt()`方法，因此另一种方法是使用`volatile`修饰一个布尔变量，并不断循环判断：
```java
public class Main {
    static class MyTask extends Thread{
        private volatile boolean closed = false;

        @Override
        public void run() {
            System.out.println("work...");
            while (!closed && !isInterrupted()){

            }
            System.out.println("exit...");
        }

        public void close(){
            this.closed = true;
            this.interrupt();
        }
    }
    public static void main(String[] args) throws InterruptedException {
        MyTask t = new MyTask();
        t.start();
        TimeUnit.SECONDS.sleep(5);
        System.out.println("System will be shutdown.");
        t.close();
    }
}
```

## 6.2 异常退出
线程执行单元中是不允许抛出`checked`异常的，如果在线程运行过程中需要捕获`checked`异常并且判断是否还有运行下去的必要，可以将`checked`异常封装为`unchecked`异常，比如`RuntimeException`，抛出从而结束线程的生命周期。

## 6.3 假死
所谓假死就是虽然线程存在，但是却没有任何的外在表现，比如：

- 没有日志输出
- 不进行任何的作业

等等，虽然此时线程是存在的，但看起来跟死了一样，事实上是没有死的，出现这种情况，很大可能是因为线程出现了阻塞，或者两个线程争夺资源出现了死锁。

这种情况需要借助一些外部工具去判断，比如`VisualVM`、`jconsole`等等，找出存在问题的线程以及当前的状态，并判断是哪个方法造成了阻塞。
