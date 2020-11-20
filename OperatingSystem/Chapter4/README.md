# Table of Contents

* [1 存储器的层次结构](#1-存储器的层次结构)
* [2 程序装入与链接](#2-程序装入与链接)
  * [2.1 程序运行步骤](#21-程序运行步骤)
  * [2.2 程序装入](#22-程序装入)
  * [2.3 程序链接](#23-程序链接)
* [3 连续分配方式](#3-连续分配方式)
  * [3.1 单一连续分配](#31-单一连续分配)
  * [3.2 固定分区分配](#32-固定分区分配)
  * [3.3 动态分区分配](#33-动态分区分配)
    * [3.3.1 基于顺序搜索的动态分区分配算法](#331-基于顺序搜索的动态分区分配算法)
    * [3.3.2 基于索引搜索的动态分区分配算法](#332-基于索引搜索的动态分区分配算法)
  * [3.4 动态可重定位分配](#34-动态可重定位分配)
* [4 对换/交换技术](#4-对换交换技术)
  * [4.1 概念](#41-概念)
  * [4.2 分类](#42-分类)
  * [4.3 进程换入与换出](#43-进程换入与换出)
* [5 分页存储](#5-分页存储)
  * [5.1 概念](#51-概念)
  * [5.2 页表](#52-页表)
  * [5.3 快表](#53-快表)
* [6 分段存储](#6-分段存储)
  * [6.1 概念](#61-概念)
  * [6.2 段表](#62-段表)
  * [6.3 分段和分页的比较](#63-分段和分页的比较)
* [7 段页式存储方式](#7-段页式存储方式)


﻿
# 1 存储器的层次结构
通常分为三级：

- 寄存器：通常指`CPU`寄存器
- 主存：包括高速缓存、主存储器和磁盘缓存
- 辅存：固定磁盘、可移动存储介质

常见的存储器：

- 主存储器：主存储器又叫内存或主存，用于保存进程运行时的程序和数据，也称为可执行存储器。
- 寄存器：寄存器具有与处理机相同的速度，完全能与`CPU`协调工作，但价格十分昂贵，寄存器字长一般为`32`或`64`位。
- 高速缓存：高速缓存是介于寄存器和存储器之间的存储器，主要用于备份主存中较常用的数据，以减少处理机对主存储器的访问次数，容量大于寄存器但比内存小。
- 磁盘缓存：为了缓和磁盘`I/O`与主存访问速度之间的矛盾，设置了磁盘缓存，用于暂时存放频繁使用的一部分磁盘数据和信息


# 2 程序装入与链接
## 2.1 程序运行步骤
一般来说，程序运行都需要经历以下步骤：

- 编译：编译器对源代码进行编译，形成若干目标文件
- 链接：链接程序将编译后的目标文件与所需要的库函数进行链接，形成一个完成的装入模块
- 装入：由装入程序将装入模块装入内存

## 2.2 程序装入
主要有三种方式：

- 绝对装入：编译后将产生绝对地址（也就是物理地址）的目标代码，接着将程序和数据装入内存，装入内存后程序中相对地址与实际内存地址完全相同
- 可重定位装入：根据内存的具体情况将装入模块装入到内存的适当位置，装入后程序的逻辑地址和实际装入内存后的物理地址不同，在装入时对目标程序中指令和数据地址的修改过程称为重定位，也叫静态重定位
- 动态运行时装入：装入内存后，不立即把装入模块中的逻辑地址转换为物理地址，在真正执行的时候才进行，装入后是逻辑地址

## 2.3 程序链接
链接也可以分为三种方式：

- 静态链接：运行之前将各个目标模块以及所需的库函数链接成一个完整的装配模块，不再拆开
- 装入时动态链接：采用边装入边链接方式，装入一个目标模块时，若发生一个外部模块调用事件，装入程序会找出响应的外部模块，并装入内存
- 运行时动态链接：将某些模块的链接推迟到运行时才执行，这样可以加快装入过程且节省内存空间


#  3 连续分配方式
主要有四种：

- 单一连续分配
- 固定分区分配
- 动态分区分配
- 动态可重定位分区分配

## 3.1 单一连续分配
单道程序环境中，内存分为：

- 系统区
- 用户区

而单一连续分配就是整个用户区内存由单个程序独占。

## 3.2 固定分区分配
将整个用户空间划分为若干个固定大小的区域，每个分区中只装入一道作业。

## 3.3 动态分区分配
根据进程实际需要，动态地分配内存空间。

其中使用到的两类重要算法为：

- 基于顺序搜索的动态分区分配算法
- 基于索引搜索的动态分区分配算法

### 3.3.1 基于顺序搜索的动态分区分配算法
可以分为：

- 首次适应算法：要求空闲分区链以地址递增的次序链接，也就是从低地址内存进行分配，逐步向高地址搜索，优点是为分配大内存创造了条件，但是会造成很多空闲碎片
- 循环首次适应：从上一次找到的空闲分区的下一个空闲分区开始查找，直到找到一个能满足要求的空闲分区，能使空闲分区分布更加均匀，但会缺乏大的空闲分区
- 最佳适应：要求所有空闲分区按容量从小到大的顺序形成一空闲分区链
- 最坏适应：与最佳适应相反，要求所有空闲分区按容量从大到小的顺序形成一空闲分区链


### 3.3.2 基于索引搜索的动态分区分配算法
可以分为：

- 快速适应算法：按空闲分区大小进行分类，同时设立一张管理索引表，每个索引表项对应一种空闲分区类型，优点是不会产生内存碎片，查找效率高，缺点是归还内存算法复杂
- 伙伴系统：每次都分配`2`的`k`次幂的可分配内存大小，比如申请`120k`，则分配`128k`的空间，申请`859k`，则分配`1024k`的空间
- 哈希算法：构造一张以空闲分区大小为关键字的哈希表，每一个表项记录了一个对应的空闲分区链表表头指针


## 3.4 动态可重定位分配
在系统中增加一个重定位寄存器，用来存放程序或数据在内存中的起始地址，在执行时，真正访问的内存地址是相对地址与重定位寄存器中的地址相加而形成的。

# 4 对换/交换技术
## 4.1 概念
对换是指把内存中暂时不能运行的进程或者暂时不用的程序和数据换出到外存上，以便腾出足够的内存空间，再把已具备运行条件的进程或进程所需要的程序和数据换入内存。
## 4.2 分类
- 整体对换：比如处理机的中级调度，实际上就是存储器的对换功能，以整个进程为单位，又叫进程对换
- 页面（分段）对换：对换以进程的一个页面或分段为单位，又叫部分对换

## 4.3 进程换入与换出
换出过程如下：

- 选择被换出的进程：检查驻留内存中的进程，同时考虑进程在内存中的驻留时间，综合选择准备换出的进程
- 进程换出：只能换出非共享的程序和数据段，在换出时，首先申请对换空间，若成功，启动磁盘并将程序和数据传送到磁盘对换区上，未出现错误则可回收进程所占用的内存空间，并修改`PCB`等数据结构

换入过程如下：

- 首先查看`PCB`集合中所有进程的状态，找出就绪状态但已换出的进程
- 申请内存
- 申请成功后调入内存
- 一直换入进程直到无“就绪且换出”的进程或者内存中无足够内存来换入进程

# 5 分页存储
## 5.1 概念
将用户程序的地址空间分为若干个固定大小的区域，称为页或者页面，典型的页面大小为`1KB`，同时也将内存空间分为若干个物理块或框，页和块的大小相同。

## 5.2 页表
页表实现的是从页号到物理块号的地址映射。

## 5.3 快表
地址变换机构的作用就是实现逻辑地址到物理地址的转换，而具有“快表”的地址变换机构能进一步提高访问速度。快表实际上就是一种页表缓冲机制，就是在高速缓存中的部分页表，如果命中了直接在快表中取出对应的页表即可，否则访问内存中的页表。


# 6 分段存储
## 6.1 概念
为了满足用户需求而形成的一种存储管理方式，把用户程序的地址空间大小分为若干个大小不同的段，每段可定义一组相对完整的信息，在存储器分配时，以段为单位。

## 6.2 段表
段表用于实现从逻辑段到物理段的映射。

## 6.3 分段和分页的比较
- 两者都采用离散分配方式，且都是通过地址变换机构实现地址变换
- 页是信息的物理单位，为了更好满足系统管理需要，段是信息的逻辑单位，为了更好满足用户需要
- 页的大小固定，且由系统决定，而段的长度不固定，取决于所编写的程序
- 分页的用户地址空间是唯一的，用户程序的地址是属于单一的线性地址空间，仅需要一个记忆符就可以标识该地址，而分段是用户行为，在分段系统中，用户程序的地址空间是二维的，程序员标识地址时需要段名以及段地址

# 7 段页式存储方式
分页系统中页页面作为内存分配的基本单位，有效提高了内存的利用率，而分段系统中以段作为内存分配的基本单位，能够更好满足用户多方面的需求，结合两者就形成了段页式存储管理方式，基本原理就是分段和分页的结合，先将用户程序分为若干个段，在把每个段分为若干个页，地址结构由段号、段内页号以及页内地址组成。

为了实现逻辑地址到物理地址的转换需要同时配置段表以及页表。