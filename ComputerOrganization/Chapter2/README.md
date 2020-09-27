# Table of Contents

* [1 二进制](#1-二进制)
* [2 进位计数法后缀](#2-进位计数法后缀)
* [3 小数进制转换](#3-小数进制转换)
  * [3.1 十进制小数转`R`进制小数](#31-十进制小数转r进制小数)
  * [3.2 `R`进制小数转十进制小数](#32-r进制小数转十进制小数)
* [4  定点与浮点](#4--定点与浮点)
  * [4.1 定点数](#41-定点数)
  * [4.2 浮点表示](#42-浮点表示)
* [5 原码、补码、反码与移码](#5-原码补码反码与移码)
  * [5.1 机器数与真值](#51-机器数与真值)
  * [5.2 原码](#52-原码)
  * [5.3 补码](#53-补码)
  * [5.4 反码](#54-反码)
  * [5.5 移码](#55-移码)
* [6 浮点数表示](#6-浮点数表示)
  * [6.1 规格化](#61-规格化)
  * [6.2 `IEEE 754`浮点数标准](#62-ieee-754浮点数标准)
  * [6.3 十进制的`IEEE 754`单精度表示](#63-十进制的ieee-754单精度表示)
  * [6.4 `IEEE 754`单精度十六进制转十进制](#64-ieee-754单精度十六进制转十进制)
* [7 非数值编码表示](#7-非数值编码表示)
  * [7.1 西文字符](#71-西文字符)
  * [7.2 `ASCII`](#72-ascii)
  * [7.3 汉字字符](#73-汉字字符)
* [8 数据校验码](#8-数据校验码)
  * [8.1 奇偶校验码](#81-奇偶校验码)
    * [8.1.1 流程](#811-流程)
    * [8.1.2 具体过程](#812-具体过程)
  * [8.2 海明校验码](#82-海明校验码)
    * [8.2.1 概述](#821-概述)
    * [8.2.2 流程](#822-流程)
    * [8.2.3 生成](#823-生成)
      * [8.2.3.1 确定校验码位数](#8231-确定校验码位数)
      * [8.2.3.2 将数据插入校验位中](#8232-将数据插入校验位中)
      * [8.2.3.3 画表](#8233-画表)
      * [8.2.3.4 计算校验位](#8234-计算校验位)
      * [8.2.3.5 生成最终结果](#8235-生成最终结果)
    * [8.2.4 纠错](#824-纠错)
  * [8.3 循环冗余校验码](#83-循环冗余校验码)
    * [8.3.1 概述](#831-概述)
    * [8.3.2 流程](#832-流程)
    * [8.3.3 生成流程](#833-生成流程)
      * [8.3.3.1 约定多项式](#8331-约定多项式)
      * [8.3.3.2 附加0](#8332-附加0)
      * [8.3.3.3 计算校验位](#8333-计算校验位)
      * [8.3.3.4 发送](#8334-发送)


# 1 二进制

计算机内部所有信息都是用二进制编码的，好处如下：

- 二进制只有两种基本状态，使用两个稳定状态的物理器件就可以表示二进制的每一位
- 二进制的编码和运算规则简单
- 两个符号正好与逻辑命题的真与假对应

# 2 进位计数法后缀
- `B`表示二进制
- `O`表示八进制
- `D`表示十进制（一般省略）
- `H`表示十六进制



# 3 小数进制转换
## 3.1 十进制小数转`R`进制小数
分两部分转换：

- 整数部分：除`R`取余数，由下往上
- 小数部分：乘`R`取整数，由上往下

比如：

```bash
135.6875
```
整数部分为`135`，小数部分为`0.6875`，整数部分的二进制表示为`128+4+2+1`，即`1000 0111`，而小数部分计算如下：

```bash
0.6875 * 2(R) = 1.375   取整1
0.375 * 2 = 0.75        取整0
0.75 * 2 = 1.5          取整1
0.5 * 2 = 1.0           取整0
小数部分为0，计算结束
```

因此小数部分为`1010`，因此表示为$(1000 0111.1010)_2$。


## 3.2 `R`进制小数转十进制小数
直接乘对应的权即可，比如
```bash
(11.110)
```
转换为十进制小数直接乘对应的权：
```bash
1 * 2^(1) + 1 *  2^(0) +
1 * 2^(-1) + 1 * 2^(-2)
```

# 4  定点与浮点
## 4.1 定点数
定点表示法用来对定点小数和定点整数进行表示。

- 对于定点小数：小数点总是固定在最左边
- 对于定点整数：小数点总是固定在最右边

## 4.2 浮点表示
任意一个二进制`X`都可以表示为如下形式：

$$X = (-1)^S × M × R^E$$

其中：

- `S`取值0或1，表示符号位
- `M`是二进制定点小数，叫`X`的尾数
- `E`是二进制定点整数，叫`X`的阶或指数
- `R`是基数



# 5 原码、补码、反码与移码

## 5.1 机器数与真值

- 机器数：计算机内部编码的数
- 真值：机器数真正的值

比如，`-10`的8位补码的表示为`11110110`，则可以说机器数`11110110`的真值为`-10` 。

## 5.2 原码

- 由符号位直接加数值构成
- 正数/负数仅符号位不同
- 原码`0`有两种表示形式

优点：

- 与真值对应关系直观、方便
- 实现乘除简单

缺点：

- `0`表示不唯一
- 加减运算复杂


## 5.3 补码
- 正数补码就是正数的原码
- 负数补码就是负数的原码除符号位外各位取反`+1`
- 定义是`X = M+XT(mod M)`，其中`X`表示补码，`XT`表示真值，`M`表示模，一般取`2^(n)`


## 5.4 反码
- 正数反码就是正数的原码
- 负数的反码就是负数的原码除符号位外各位取反


## 5.5 移码
- 移码一般用于表示浮点数指数
- 需要一个偏置常数
- `E = 2^(n-1)+ET`，其中`E`表示移码，`2^(n-1)`为偏置常数，`ET`表示真值



# 6 浮点数表示
## 6.1 规格化 
- 目的是保留尽可能多的有效数字位数，也就是使有效数字尽量占满尾数位
- 规格化的标志是真值的尾数部分最高位具有非零数字
- 规格化可以分为左规和右规

## 6.2 `IEEE 754`浮点数标准

- 单精度标准：`1位`符号，`8位`阶码，`23位`尾数，合计`32位`
- 双精度标准：`1位`符号，`11位`阶码，`52位`尾数，合计`64位`

标准规定：

- **尾数用原码表示，第一位总为`1`，而且可在尾数中省略第一位的`1`，称为隐藏位**
- **指数用移码表示，单精度的偏置常数是`127`，双精度为`1023`**

## 6.3 十进制的`IEEE 754`单精度表示
步骤：

- 十进制化成二进制小数
- 规格化
- 先写符号位
- 指数加上偏置常数并添加在符号位后面
- 尾数直接加在指数后面
- 每隔四位分割，用十六进制表示，加上`H`

比如`+1.625`：

- 二进制小数为`1.101`，也就是`1.101 * 2^(0)`
- 无需规则化，因为最高位为`1`
- 符号位`0`，正数
- 指数需要加上偏置常数`127`，也就是`0+127=127=0111 1111`
- 尾数直接补充在指数后面即可
- 因此最终答案为`0(符号位)     0111 1111(指数)       101(尾数)`，最后在后面补足`0`，补到`32`位
- 也就是`0011   1111   1101   0000   0000   0000   0000   0000`
- 每四位用十六进制表示，就是`3FD0 000H`


## 6.4 `IEEE 754`单精度十六进制转十进制
步骤：

- 化为`32位`二进制
- 抽离符号位，也就是第一位，`0`表示正数，`1`表示负数
- 继续抽离`8位`，表示指数，需要减去偏置常数`127`，先把`8位`二进制化为十进制再减去`127`，得到真正的指数，注意该指数以`2`为底
- 尾数直接按小数形式转换为十进制，也就是每位乘以对应的权，最后加上整数部分`1`
- 最终的表示为`尾数 * 2^(指数)`，注意指数以`2`为底，最后相乘后得到十进制数

比如`4510 000H`：

- 二进制为：`0100 0101 0001 0000    0000 0000 0000 0000`
- 分割，分成`1+8+23`的形式，`0     1000 1010      001 0000 0000 0000 0000 0000`
- 抽离符号位，也就是第一位，`0`表示正数
- 继续抽离`8`位表示指数，`1000 1010 = 128+8+2`，再减去偏置常数`127`，也就是`11`
- 尾数需要加上隐藏位`1`，也就是变成`1.001 0000 ..... 0000`，转换为十进制为`1.125`
- 最后为`1.125 × 2^(11)`，也就是`2304`


# 7 非数值编码表示
## 7.1 西文字符
- 由拉丁字母、数字、标点符号记忆一些特殊符号组成
- 最广泛的西文字符集为`ASCII`

## 7.2 `ASCII`
- 每个字符由7个二进制位组成
- 一般情况下最高位为`0`
- 需要奇偶校验时，最高位可用于存放奇偶校验值

## 7.3 汉字字符
- 1981年颁布`GB2312`国标字符集，这个标准叫国标码，又叫国标交换码
- `GB2312`中码表由`94`行+`94`列组成
- 行号称为区号
- 列号称为位号
- `7位`区号在左，`7位`区号在右，`14位`代码就叫汉字的区位码


# 8 数据校验码
三种：

- 奇偶校验码
- 海明校验码
- 循环冗余校验码

## 8.1 奇偶校验码
### 8.1.1 流程

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200926144543275.png)

### 8.1.2 具体过程
假设：

- 原数据：$b_n,b_{n-1},...,b_0$
- 原数据校验码：$P_1$
- 接收到的数据：$c_n,c_{n-1},...,c_0$
- 接受到的校验码：$C_{P1}$

目标就是验证从接收到的数据$c_n,c_{n-1},...,c_0$计算出来的校验码$P_2$与从原数据接受过来的校验码$C_{P1}$（因为传输过程中有可能校验码错误，所以不能用同一个字母$P_1$表示）进行比较看是否相等。

校验码计算流程如下：

- 若采用奇校验位，则$P = b_n @ b_{n-1} @ ... @ b_0 @ 1$，其中$@$表示异或操作
- 若采用偶校验位，则$P = b_n @ b_{n-1} @ ... @ b_0$

完整过程：

- 对原数据计算校验码$P_1$
- 原数据与原数据校验码一起发送
- 接收到数据后，计算接收到的数据的校验码$P_2$
- 比较原数据的校验码$C_{P1}$以及$P_2$，两者进行异或比较
- 若结果为1，表示有奇数位出错
- 若结果为0，表示没有出错或有偶数位出错



## 8.2 海明校验码
### 8.2.1 概述
海明校验码和奇偶校验码不同，奇偶校验码只能检测是否出错，而没有纠正错误的能力，而海明校验码除了能够监测是否出错，还知道出错的位置以及如何对错误进行纠正。因此，海明校验码分为两步：

- 生成
- 纠错

### 8.2.2 流程
生成过程如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200926145840380.png)

纠错过程如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200926150249628.png)

### 8.2.3 生成
#### 8.2.3.1 确定校验码位数
首先对数据的校验位的位数进行确认，因为数据以及校验位都是以二进制形式表示，假设数据有$n$位，那么校验位应该能唯一地表示以下情况：

- `n`表示每一位上的数据出错的情况，因为有`n`位数据，所以出错的可能情况为`n`种
- `1`表示所有数据都不出错的情况
- 因此对应的情况为`n+1`种

也就是说，对于数据来说，校验位需要表示`n+1`种情况。

而对于校验位本身来说，假设校验位有$k$位，那么：

- 出错的可能情况为`k`种，因为每一位都是0或1， 只有两种取值，有$k$位
- 约定全0表示校验位正确

因此，对校验位本身需要表示`k`种情况，也就是说：

- 如果数据有$n$位
- 那么设校验位有$k$位
- 需要符合$2^k >= n+k+1$


因此校验码的位数由数据的位数确定，比如数据有4位，那么校验位至少也需要4位。


#### 8.2.3.2 将数据插入校验位中
首先引入一个叫故障字的概念，故障字实质上就是校验位的组合，比如校验位有4位，分别是`1 0 1 0`，那么故障字就是把它们拼起来，也就是`1010`，这个就是故障字。

对于故障字有如下约定：

- 故障字全0表示没有错误
- 故障字有且仅有一位1，表示其中一位校验位出错，而数据没有错误，故不需要纠错
- 故障字中有两位（包括两位）以上的1，表明数据出错，只需要对出错位数据进行取反


解释如下：

- 从故障字的定义可以看到，故障字位数与校验位的位数相同
- 因此，可以认为定义为1表示出错，0表示正确
- 由于故障字与校验位一一对应的关系，若故障字中第1位为1 ，则表示第1位校验位出错，故障字第2位为1，表示第2位校验位出错

也就说故障字与校验位对应关系如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200926152252541.png)


故障字`0100`表示校验位`P3`出错，另外，把`0100`看作二进制数，则十进制表示为`4`，也就是：

- `P1`出错：故障字为1
- `P2`出错：故障字为2
- `P3`出错：故障字为4
- `P4`出错：故障字为8

下一步就是将数据插入到对应的空位中，假设有8位数据，分别为为$M_8,M_7,M_6,...,M1$，上面的故障字没有对应数字3， 那么就可以将$M_1$插入到故障字$P_2$与$P_4$中，让它可以唯一地表示故障字3 ，同样道理，因为$P_3$与$P_4$之间有三个空缺数字，所以可以插入$M_4,M_3,M2$，因此完整的表示如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200926153546878.png)

上面的图实际上是故障字错误表示的位数，也就是，故障字为8 ，表示$P_4$出错，故障字为5，表示$M_2$出错。

#### 8.2.3.3 画表
下一步就是画出故障字与出错情况关系的对应表，实际上就是把上面那个图用二进制形式表示一下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200926154323670.png)

表下的数字就是对应的二进制数。

#### 8.2.3.4 计算校验位
根据上一步得到的表，计算$k$位校验位，规则如下：

- 对于每个校验位，查相同的行中数据为1的位置，将对应的数据位进行异或
- 比如计算校验位$P_1$，$P_1$在最后一行，在$M_1,M_2,M_4,M5,M_7$下都有1，因此$P_1 = M_1 @ M_2 @ M_4 @ M_5 @ M_7$，其中$@$表示异或


#### 8.2.3.5 生成最终结果
对于每个校验位按上面的方式计算后，得到所有校验位，附在数据的后面直接发送即可，也就是发送`n+k`位数据。

### 8.2.4 纠错
纠错比生成要简单，只需要上面的那一个故障字与出错情况对应表即可。具体流程如下：

- 将收到的数据进行校验位的计算
- 将收到的校验位与新计算出来的校验位进行异或
- 异或出来的就是故障字，根据故障字与出错情况对应表可以知道哪一位出错，对应的修正方法就是将出错的位取反，就是表示正确的数据


## 8.3 循环冗余校验码
### 8.3.1 概述
循环冗余校验码（`CRC`码）是以模2除法为基础的具有出错检测以及纠错功能的一种校验码，`CRC`码组成如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200926155613498.png)

### 8.3.2 流程
生成的流程如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200926160151712.png)

至于纠错流程，虽然`CRC`码能纠错，但是一般只是用于检测错误，因此对纠错部分不作介绍。

### 8.3.3 生成流程
#### 8.3.3.1 约定多项式
首先需要发送与接收双方约定一个多项式，比如多项式为$x^3 + 1$，则表示多项式位数为4位，而校验位数为多项式位数减1，也就是校验位位数为3位。另外，多项式一般用二进制表示，比如上面的多项式表示为`1001`，`1001`表示$1*x^3 + 0*x^2 + 0*x^1 + 1*x^0$，也就是$x^3 + 1$。

#### 8.3.3.2 附加0
计算校验位之前需要在原数据后面附加“校验位个数”个0 ，比如上面的$x^3+1$，校验位个数为3（多项式位数减1），则需要在原数据后面附加3个0，为下一步的计算校验位作准备。

#### 8.3.3.3 计算校验位
计算的方式是模2除法，听起来很高级实际上就是两句话：

- 相同=0
- 不同=1

也就是说相当与异或操作，比如`1000`除以`1001`，前三位相同，相同就是0，最后一位不同，不同就是1，因此结果是`0001`。

因此，假设数据是`100011`，约定的多项式为`1001`， 校验位位数为3位，计算流程如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200926161747736.jpg)

也就是校验位为`111`。

#### 8.3.3.4 发送
将校验位`111`附加到数据的后面，也就是发送的数据为`100011111`。





