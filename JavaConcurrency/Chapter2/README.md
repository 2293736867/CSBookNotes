# Table of Contents

* [1 来源](#1-来源)
* [2 概述](#2-概述)
* [3 `synchronized`](#3-synchronized)
  * [3.1 简介](#31-简介)
  * [3.2 基本用法](#32-基本用法)
  * [3.3 字节码简单分析](#33-字节码简单分析)
  * [3.4 注意事项](#34-注意事项)
    * [3.4.1 非空对象](#341-非空对象)
    * [3.4.2 作用域不当](#342-作用域不当)
    * [3.4.3 使用不同的对象](#343-使用不同的对象)
  * [3.5 死锁](#35-死锁)
    * [3.5.1 死锁成因](#351-死锁成因)
    * [3.5.2 例子](#352-例子)
    * [3.5.3 排查](#353-排查)
  * [3.6 两个特殊的`monitor`](#36-两个特殊的monitor)
    * [3.6.1 `this monitor`](#361-this-monitor)
    * [3.6.2 `class monitor`](#362-class-monitor)
    * [3.6.3 总结](#363-总结)
* [4 `ThreadGroup`](#4-threadgroup)
  * [4.1 简介](#41-简介)
  * [4.2 创建](#42-创建)
  * [4.3 `enumerate()`](#43-enumerate)
  * [4.4 其他`API`](#44-其他api)


# 1 来源

- 来源：《Java高并发编程详解 多线程与架构设计》，汪文君著
- 章节：第四、六章

本文是两章的笔记整理。

# 2 概述
本文主要讲述了`synchronized`以及`ThreadGroup`的基本用法。

# 3 `synchronized`
## 3.1 简介
`synchronized`可以防止线程干扰和内存一致性错误，具体表现如下：

- `synchronized`提供了一种锁机制，能够确保共享变量的互斥访问，从而防止数据不一致的问题
- `synchronized`包括`monitor enter`和`monitor exit`两个`JVM`指令，能保证在任何时候任何线程执行到`monitor enter`成功之前都必须从主存获取数据，而不是从缓存中，在`monitor exit`运行成功之后，共享变量被更新后的值必须刷入主内存而不是仅仅在缓存中
- `synchronized`指令严格遵循`Happens-Beofre`规则，一个`monitor exit`指令之前必定要有一个`monitor enter`

## 3.2 基本用法
`synchronized`的基本用法可以用于对代码块或方法进行修饰，比如：
```java
private final Object MUTEX = new Object();
    
public void sync1(){
    synchronized (MUTEX){
    }
}

public synchronized void sync2(){
}
```

## 3.3 字节码简单分析
一个简单的例子如下：
```java
public class Main {
    private static final Object MUTEX = new Object();

    public static void main(String[] args) throws InterruptedException {
        final Main m = new Main();
        for (int i = 0; i < 5; i++) {
            new Thread(m::access).start();
        }
    }

    public void access(){
        synchronized (MUTEX){
            try{
                TimeUnit.SECONDS.sleep(20);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}
```
编译后查看字节码：
```bash
javap -v -c -s -l Main.class
```
`access()`字节码截取如下：
```cpp
stack=3, locals=4, args_size=1
 0: getstatic     #9                  // Field MUTEX:Ljava/lang/Object;  获取MUTEX
 3: dup
 4: astore_1
 5: monitorenter					  // 执行monitor enter指令
 6: getstatic     #10                 // Field java/util/concurrent/TimeUnit.SECONDS:Ljava/util/concurrent/TimeUnit;
 9: ldc2_w        #11                 // long 20l
12: invokevirtual #13                 // Method java/util/concurrent/TimeUnit.sleep:(J)V
15: goto          23				  // 正常退出，跳转到字节码偏移量23的地方
18: astore_2
19: aload_2
20: invokevirtual #15                 // Method java/lang/InterruptedException.printStackTrace:()V
23: aload_1
24: monitorexit						  // monitor exit指令
25: goto          33
28: astore_3
29: aload_1
30: monitorexit
31: aload_3
32: athrow
33: return
```
关于`monitorenter`与`monitorexit`说明如下：

- `monitorenter`：每一个对象与一个`monitor`相对应，一个线程尝试获取与对象关联的`monitor`的时候，如果`monitor`的计数器为0，会获得之后立即对计数器加1，如果一个已经拥有`monitor`所有权的线程重入，将导致计数器再次累加，而如果其他线程尝试获取时，会一直阻塞直到`monitor`的计数器变为0，才能再次尝试获取对`monitor`的所有权
- `monitorexit`：释放对`monitor`的所有权，将`monitor`的计数器减1，如果计数器为0，意味着该线程不再拥有对`monitor`的所有权

## 3.4 注意事项
### 3.4.1 非空对象
与`monitor`关联的对象不能为空：
```java
private Object MUTEX = null;
private void sync(){
    synchronized (MUTEX){

    }
}
```
会直接抛出空指针异常。

### 3.4.2 作用域不当
由于`synchronized`关键字存在排它性，作用域越大，往往意味着效率越低，甚至丧失并发优势，比如：
```java
private synchronized void sync(){
    method1();
    syncMethod();
    method2();
}
```
其中只有第二个方法是并发操作，那么可以修改为
```java
private Object MUTEX = new Object();
private void sync(){
    method1();
    synchronized (MUTEX){
        syncMethod();
    }
    method2();
}
```

### 3.4.3 使用不同的对象
因为一个对象与一个`monitor`相关联，如果使用不同的对象，这样就失去了同步的意义，例子如下：
```java
public class Main {
    public static class Task implements Runnable{
        private final Object MUTEX = new Object();

        @Override
        public void run(){
            synchronized (MUTEX){
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 20; i++) {
            new Thread(new Task()).start();
        }
    }
}
```
每一个线程争夺的`monitor`都是互相独立的，这样就失去了同步的意义，起不到互斥的作用。

## 3.5 死锁
另外，使用`synchronized`还需要注意的是有可能造成死锁的问题，先来看一下造成死锁可能的原因。

### 3.5.1 死锁成因
- 交叉锁导致程序死锁：比如线程A持有R1的锁等待R2的锁，线程B持有R2的锁等待R1的锁
- 内存不足：比如两个线程T1和T2，T1已获取10MB内存，T2获取了15MB内存，T1和T2都需要获取30MB内存才能工作，但是剩余可用的内存为10MB，这样两个线程都在等待彼此释放内存资源
- 一问一答式的数据交换：服务器开启某个端口，等待客户端访问，客户端发送请求后，服务器因某些原因错过了客户端请求，导致客户端等待服务器回应，而服务器等待客户端发送请求
- 死循环引起的死锁：比较常见，使用`jstack`等工具看不到死锁，但是程序不工作，`CPU`占有率高，这种死锁也叫系统假死，难以排查和重现

### 3.5.2 例子
```java
public class Main {
    private final Object MUTEX_READ = new Object();
    private final Object MUTEX_WRITE = new Object();

    public void read(){
        synchronized (MUTEX_READ){
            synchronized (MUTEX_WRITE){
            }
        }
    }

    public void write(){
        synchronized (MUTEX_WRITE){
            synchronized (MUTEX_READ){
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Main m = new Main();
        new Thread(()->{
            while (true){
                m.read();
            }
        }).start();
        new Thread(()->{
            while (true){
                m.write();
            }
        }).start();
    }
}
```
两个线程分别占有`MUTEX_READ`/`MUTEX_WRITE`，同时等待另一个线程释放`MUTEX_WRITE`/`MUTEX_READ`，这就是交叉锁造成的死锁。

### 3.5.3 排查
使用`jps`找到进程后，通过`jstack`查看：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210509221644115.png)

可以看到明确的提示找到了1个死锁，`Thread-0`等待被`Thread-1`占有的`monitor`，而`Thread-1`等待被`Thread-0`占有的`monitor`。

## 3.6 两个特殊的`monitor`
这里介绍两个特殊的`monitor`：

- `this monitor`
- `class monitor`

### 3.6.1 `this monitor`
先上一段代码：
```java
public class Main {
    public synchronized void method1(){
        System.out.println(Thread.currentThread().getName()+" method1");
        try{
            TimeUnit.MINUTES.sleep(5);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public synchronized void method2(){
        System.out.println(Thread.currentThread().getName()+" method2");
        try{
            TimeUnit.MINUTES.sleep(5);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Main m = new Main();
        new Thread(m::method1).start();
        new Thread(m::method2).start();
    }
}
```
运行之后可以发现，只有一行输出，也就是说，只是运行了其中一个方法，另一个方法根本没有执行，使用`jstack`可以发现：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210509222610580.png)

一个线程处于休眠中，而另一个线程处于阻塞中。而如果将`method2()`修改如下：
```java
public void method2(){
    synchronized (this) {
        System.out.println(Thread.currentThread().getName() + " method2");
        try {
            TimeUnit.MINUTES.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```
效果是一样的。也就是说，在方法上使用`synchronized`，等价于`synchronized(this)`。

### 3.6.2 `class monitor`
把上面的代码中的方法修改为静态方法：
```java
public class Main {
    public static synchronized void method1() {
        System.out.println(Thread.currentThread().getName() + " method1");
        try {
            TimeUnit.MINUTES.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void method2() {
        System.out.println(Thread.currentThread().getName() + " method2");
        try {
            TimeUnit.MINUTES.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new Thread(Main::method1).start();
        new Thread(Main::method2).start();
    }
}
```
运行之后可以发现输出还是只有一行，也就是说只运行了其中一个方法，`jstack`分析也类似：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210509223432245.png)

而如果将`method2()`修改如下：
```java
public static void method2() {
    synchronized (Main.class) {
        System.out.println(Thread.currentThread().getName() + " method2");
        try {
            TimeUnit.MINUTES.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```
可以发现输出还是一致，也就是说，在静态方法上的`synchronized`，等价于`synchronized(XXX.class)`。

### 3.6.3 总结
- `this monitor`：在成员方法上的`synchronized`，就是`this monitor`，等价于在方法中使用`synchronized(this)`
- `class monitor`：在静态方法上的`synchronized`，就是`class monitor`，等价于在静态方法中使用`synchronized(XXX.class)`

# 4 `ThreadGroup`
## 4.1 简介
无论什么情况下，一个新创建的线程都会加入某个`ThreadGroup`中：

- 如果新建线程没有指定`ThreadGroup`，默认就是`main`线程所在的`ThreadGroup`
- 如果指定了`ThreadGroup`，那么就加入该`ThreadGroup`中

`ThreadGroup`中存在父子关系，一个`ThreadGroup`可以存在子`ThreadGroup`。

## 4.2 创建
创建`ThreadGroup`可以直接通过构造方法创建，构造方法有两个，一个是直接指定名字（`ThreadGroup`为`main`线程的`ThreadGroup`），一个是带有父`ThreadGroup`与名字的构造方法：
```java
ThreadGroup group1 = new ThreadGroup("name");
ThreadGroup group2 = new ThreadGroup(group1,"name2");
```
完整例子：
```java
public static void main(String[] args) throws InterruptedException {
    ThreadGroup group1 = new ThreadGroup("name");
    ThreadGroup group2 = new ThreadGroup(group1,"name2");
    System.out.println(group2.getParent() == group1);
    System.out.println(group1.getParent().getName());
}
```
输出结果：
```bash
true
main
```

## 4.3 `enumerate()`
`enumerate()`可用于`Thread`和`ThreadGroup`的复制，因为一个`ThreadGroup`可以加入若干个`Thread`以及若干个子`ThreadGroup`，使用该方法可以方便地进行复制。方法描述如下：

- `public int enumerate(Thread [] list)`
- `public int enumerate(Thread [] list, boolean recurse)`
- `public int enumerate(ThreadGroup [] list)`
- `public int enumerate(ThreadGroup [] list, boolean recurse)`

上述方法会将`ThreadGroup`中的活跃线程/`ThreadGroup`复制到`Thread`/`ThreadGroup`数组中，布尔参数表示是否开启递归复制。

例子如下：
```java
public static void main(String[] args) throws InterruptedException {
    ThreadGroup myGroup = new ThreadGroup("MyGroup");
    Thread thread = new Thread(myGroup,()->{
        while (true){
            try{
                TimeUnit.SECONDS.sleep(1);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    },"MyThread");
    thread.start();
    TimeUnit.MILLISECONDS.sleep(1);
    ThreadGroup mainGroup = currentThread().getThreadGroup();
    Thread[] list = new Thread[mainGroup.activeCount()];
    int recurseSize = mainGroup.enumerate(list);
    System.out.println(recurseSize);
    recurseSize = mainGroup.enumerate(list,false);
    System.out.println(recurseSize);
}
```
后一个输出比前一个少1，因为不包含`myGroup`中的线程（递归设置为`false`）。需要注意的是，`enumerate()`获取的线程仅仅是一个预估值，并不能百分百地保证当前`group`的活跃线程，比如调用复制之后，某个线程结束了生命周期或者新的线程加入进来，都会导致数据不准确。另外，返回的`int`值相较起`Thread[]`的长度更为真实，因为`enumerate`仅仅将当前活跃的线程分别放进数组中，而返回值`int`代表的是真实的数量而不是数组的长度。

## 4.4 其他`API`

- `activeCount()`：获取`group`中活跃的线程，估计值
- `activeGroupCount()`：获取`group`中活跃的子`group`，也是一个近似值，会递归获取所有的子`group`
- `getMaxPriority()`：用于获取`group`的优先级，默认情况下，`group`的优先级为10，且所有线程的优先级不得大于线程所在`group`的优先级
- `getName()`：获取`group`名字
- `getParent()`：获取父`group`，如果不存在返回`null`
- `list()`：一个输出方法，递归输出所有活跃线程信息到控制台
- `parentOf(ThreadGroup g)`：判断当前`group`是不是给定`group`的父`group`，如果给定的`group`是自己本身，也会返回`true`
- `setMaxPriority(int pri)`：指定`group`的最大优先级，设定后也会改变所有子`group`的最大优先级，另外，修改优先级后会出现线程优先级大于`group`优先级的情况，比如线程优先级为10，设置`group`优先级为5后，线程优先级就大于`group`优先级，但是新加入的线程优先级必须不能大于`group`优先级
- `interrupt()`：导致所有的活跃线程被中断，递归调用线程的`interrupt()`
- `destroy()`：如果没有任何活跃线程，调用后在父`group`中将自己移除
- `setDaemon(boolean daemon)`：设置为守护`ThreadGroup`后，如果该`ThreadGroup`没有任何活跃线程，自动被销毁
