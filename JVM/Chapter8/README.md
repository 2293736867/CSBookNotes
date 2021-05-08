# Table of Contents

* [1 来源](#1-来源)
* [2 概述](#2-概述)
* [3 类加载流程](#3-类加载流程)
  * [3.1 类加载条件](#31-类加载条件)
  * [3.2 加载](#32-加载)
  * [3.3 验证](#33-验证)
  * [3.4 准备](#34-准备)
  * [3.5 解析](#35-解析)
  * [3.6 初始化](#36-初始化)
* [4 `ClassLoader`](#4-classloader)
  * [4.1 `ClassLoader`简介](#41-classloader简介)
  * [4.2 类加载器分类](#42-类加载器分类)
  * [4.3 双亲委派](#43-双亲委派)


# 1 来源
- 来源：《Java虚拟机 JVM故障诊断与性能优化》——葛一鸣
- 章节：第十章

本文是第十章的一些笔记整理。

# 2 概述
本文主要讲述了类加载器以及类加载的详细流程。

# 3 类加载流程
类加载的流程可以简单分为三步：

- 加载
- 连接
- 初始化

而其中的连接又可以细分为三步：

- 验证
- 准备
- 解析

下面会分别对各个流程进行介绍。

## 3.1 类加载条件
在了解类接在流程之前，先来看一下触发类加载的条件。

`JVM`不会无条件加载类，只有在一个类或接口在初次使用的时候，必须进行初始化。这里的使用是指主动使用，主动使用包括如下情况：

- 创建一个类的实例的时候：比如使用`new`创建，或者使用反射、克隆、反序列化
- 调用类的静态方法的时候：比如使用`invokestatic`指令
- 使用类或接口的静态字段：比如使用`getstatic`/`putstatic`指令
- 使用`java.lang.reflect`中的反射类方法时
- 初始化子类时，要求先初始化父类
- 含有`main()`方法的类

除了以上情况外，其他情况属于被动使用，不会引起类的初始化。

比如下面的例子：
```java
public class Main {
    public static void main(String[] args){
        System.out.println(Child.v);
    }
}

class Parent{
    static{
        System.out.println("Parent init");
    }
    public static int v = 100;
}

class Child extends Parent{
    static {
        System.out.println("Child init");
    }
}
```
输出如下：
```bash
Parent init
100
```
而加上类加载参数`-XX:+TraceClassLoading`后，可以看到`Child`确实被加载了：
```bash
[0.068s][info   ][class,load] com.company.Main
[0.069s][info   ][class,load] com.company.Parent
[0.069s][info   ][class,load] com.company.Child
Parent init
100
```
但是并没有进行初始化。另外一个例子是关于`final`的，代码如下：
```java
public class Main {
    public static void main(String[] args){
        System.out.println(Test.STR);
    }
}

class Test{
    static{
        System.out.println("Test init");
    }
    public static final String STR = "Hello";
}
```
输出如下：
```bash
[0.066s][info   ][class,load] com.company.Main
Hello
```
`Test`类根本没有被加载，因为`final`被做了优化，编译后的`Main.class`中，并没有引用`Test`类：
```bash
0: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
3: ldc           #4                  // String Hello
5: invokevirtual #5                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
```
在字节码偏移3的位置，通过`ldc`将常量池第4项入栈，此时在字节码文件中常量池第4项为：
```bash
#3 = Class              #24            // com/company/Test
#4 = String             #25            // Hello
#5 = Methodref          #26.#27        // java/io/PrintStream.println:(Ljava/lang/String;)V
```
因此并没有对`Test`类进行加载，只是直接引用常量池中的常量，因此输出没有`Test`的加载日志。

## 3.2 加载
类加载的时候，`JVM`必须完成以下操作：

- 通过类的全名获取二进制数据流
- 解析类的二进制数据流为方法区内的数据结构
- 创建`java.lang.Class`类的实例，表示该类型

第一步获取二进制数据流，途径有很多，包括：

- 字节码文件
- `JAR`/`ZIP`压缩包
- 从网络加载

等等，获取到二进制数据流后，`JVM`进行处理并转化为一个`java.lang.Class`实例。

## 3.3 验证
验证的操作是确保加载的字节码是合法、合理并且规范的。步骤简略如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210504112255540.png)

- 格式检查：判断二进制数据是否符合格式要求和规范，比如是否以魔数开头，主版本号和小版本号是否在当前`JVM`支持范围内等等
- 语义检查：比如是否所有类都有父类存在，一些被定义为`final`的方法或类是否被重载了或者继承了，是否存在不兼容方法等等
- 字节码验证：会试图通过对字节码流的分析，判断字节码是否可以正确被执行，比如是否会跳转到一条不存在的指令，函数调用是否传递了正确的参数等等，但是却无法100%判断一段字节码是否可以被安全执行，只是尽可能检查出可以预知的明显问题。如果无法通过检查，则不会加载这个类，如果通过了检查，也不能说明这个类完全没有问题
- 符号引用验证：检查类或方法是否确实存在，并且确定当前类有没有权限访问这些数据，比如无法找到一个类就抛出`NoClassDefFoundError`，无法找到方法就抛出`NoSuchMethodError`

## 3.4 准备
类通过验证后，就会进入准备阶段，在这个阶段，`JVM`为会类分配相应的内存空间，并设置初始值，比如：

- `int`初始化为`0`
- `long`初始化为`0L`
- `double`初始化为`0f`
- 引用初始化为`null`

如果存在常量字段，那么这个阶段也会为常量赋值。
## 3.5 解析
解析就是将类、接口、字段和方法的符号引用转为直接引用。符号引用就是一些字面量引用，和`JVM`的内存数据结构和内存布局无关，由于在字节码文件中，通过常量池进行了大量的符号引用，这个阶段就是将这些引用转为直接引用，得到类、字段、方法在内存中的指针或直接偏移量。

另外，由于字符串有着很重要的作用，`JVM`对`String`进行了特别的处理，直接使用字符串常量时，就会在类中出现`CONSTANT_String`，并且会引用一个`CONSTANT_UTF8`常量项。`JVM`运行时，内部的常量池中会维护一张字符串拘留表（`intern`），会保存其中出现过的所有字符串常量，并且没有重复项。使用`String.intern()`可以获得一个字符串在拘留表的引用，比如下面代码：
```java
public static void main(String[] args){
    String a = 1 + String.valueOf(2) + 3;
    String b = "123";
    System.out.println(a.equals(b));
    System.out.println(a == b);
    System.out.println(a.intern() == b);
}
```
输出：
```bash
true
false
true
```
这里`b`就是常量本身，因此`a.intern()`返回在拘留表的引用后就是`b`本身，比较结果为真。

## 3.6 初始化
初始化阶段会执行类的初始化方法`<clint>`，`<clint>`是由编译期生成的，由静态成员的赋值语句以及`static`语句共同产生。

另外，加载一个类的时候，`JVM`总是会试图加载该类的父类，因此父类的`<clint>`方法总是在子类的`<clint>`方法之前被调用。另一方面，需要注意的是`<clint>`会确保在多线程环境下的安全性，也就是多个线程同时初始化同一个类时，只有一个线程可以进入`<clint>`方法，换句话说，在多线程下可能会出现死锁，比如下面代码：
```java
package com.company;

import java.util.concurrent.TimeUnit;

public class Main extends Thread{
    private char flag;
    public Main(char flag){
        this.flag = flag;
    }
    
    public static void main(String[] args){
        Main a = new Main('A');
        a.start();
        Main b = new Main('B');
        b.start();
    }

    @Override
    public void run() {
        try{
            Class.forName("com.company.Static"+flag);
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }
}

class StaticA{
    static {
        try {
            TimeUnit.SECONDS.sleep(1);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        try{
            Class.forName("com.company.StaticB");
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        System.out.println("StaticA init ok");
    }
}

class StaticB{
    static {
        try {
            TimeUnit.SECONDS.sleep(1);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        try{
            Class.forName("com.company.StaticA");
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        System.out.println("StaticB init ok");
    }
}
```
在加载`StaticA`的时候尝试加载`StaticB`，但是由于`StaticB`已经被加载中，因此加载`StaticA`的线程会阻塞在`Class.forName("com.company.StaticB")`处，同理加载`StaticB`的线程会阻塞在`Class.forName("com.company.StaticA")`处，这样就出现死锁了。
# 4 `ClassLoader`
## 4.1 `ClassLoader`简介
`ClassLoader`是类加载的核心组件，所有的`Class`都是由`ClassLoader`加载的，`ClassLoader`通过各种各样的方式将`Class`信息的二进制数据流读入系统，然后交给`JVM`进行连接、初始化等操作。因此`ClassLoader`负责类的加载流程，无法通过`ClassLoader`改变类的连接和初始化行为。

`ClassLoader`是一个抽象类，提供了一些重要接口定义加载流程和加载方式，主要方法如下：

- `public Class<?> loadClass(String name) throws ClassNotFoundException`：给定一个类名，加载一个类，返回这个类的`Class`实例，找不到抛出异常
- `protected final Class<?> defineClass(byte[] b, int off, int len)`：根据给定字节流定义一个类，`off`和`len`表示在字节数组中的偏移和长度，这是一个`protected`方法，在自定义子类中才能使用
- `protected Class<?> findClass(String name) throws ClassNotFoundException`：查找一个类，会在`loadClass`中被调用，用于自定义查找类的逻辑
- `protected Class<?> findLoadedClass(String name)`：寻找一个已经加载的类

## 4.2 类加载器分类
在标准的`Java`程序中，`JVM`会创建3类加载器为整个应用程序服务，分别是：

- 启动类加载器：`Bootstrap ClassLoader`
- 扩展类加载器：`Extension ClassLoader`
- 应用类加载器（也叫系统类加载器）：`App ClassLoader`

另外，在程序中还可以定义自己的类加载器，从总体看，层次结构如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210504143259172.png)

一般来说各个加载器负责的范围如下：

- 启动类加载器：负责加载系统的核心类，比如`rt.jar`包中的类
- 扩展类加载器：负责加载`lib/ext/*.jar`下的类
- 应用类加载器：负责加载用户程序的类
- 自定义加载器：加载一些特殊途径的类，一般是用户程序的类

## 4.3 双亲委派
默认情况下，类加载使用双亲委派加载的模式，具体来说，就是类在加载的时候，会判断当前类是否已经被加载，如果已经被加载，那么直接返回已加载的类，如果没有，会先请求双亲加载，双亲也是按照一样的流程先判断是否已加载，如果没有在此委托双亲加载，如果双亲加载失败，则会自己加载。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210504143259172.png)

在上图中，应用类加载器的双亲为扩展类加载器，扩展类加载器的双亲为启动类加载器，当系统需要加载一个类的时候，会先从底层类加载器开始进行判断，当需要加载的时候会从顶层开始加载，依次向下尝试直到加载成功。

在所有加载器中，启动类加载器是最特别的，并不是使用`Java`语言实现，在`Java`中没有对象与之相对应，系统核心类就是由启动类加载器进行加载的。换句话说，如果尝试在程序中获取启动类加载器，得到的值是`null`：
```java
System.out.println(String.class.getClassLoader() == null);
```
输出结果为真。
