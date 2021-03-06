# Table of Contents

* [1 黑盒测试概述](#1-黑盒测试概述)
* [2 等价类划分](#2-等价类划分)
  * [2.1 等价类](#21-等价类)
  * [2.2 划分原则](#22-划分原则)
* [3 边界值分析](#3-边界值分析)
  * [3.1 边界值分析设计原则](#31-边界值分析设计原则)
  * [3.2 边界分析的两种方法](#32-边界分析的两种方法)
* [4 决策表](#4-决策表)
* [5 因果图](#5-因果图)
  * [5.1 定义](#51-定义)
  * [5.2 基本图形符号](#52-基本图形符号)
* [6 场景法](#6-场景法)

# 1 黑盒测试概述
黑盒测试也叫功能测试，通过测试来检测每个功能是否都能正常使用。在测试中，把程序看作是一个不能打开的黑盒子，在完全不考虑程序内部结构和内部特性的情况下，对程序接口进行测试，只检查程序功能是否按照需求规格说明书的规定正常使用，程序是否能适当接收输入数据而产生正确的输出信息。

黑盒测试以用户角度，从输入数据与输出数据的关系触发，试图发现一下几类错误：

- 功能不正确或遗漏
- 界面错误
- 数据库访问错误
- 性能错误
- 初始化和终止错误

用例设计方法包括：

- 等价类划分
- 边界值分析
- 决策表
- 因果图
- 场景法

等等。

# 2 等价类划分
## 2.1 等价类
等价类是指某个输入域的子集，在该子集合中，测试某等价类的代表值就等于这一类其他值的测试，对于揭露程序的错误是等效的。因此，全部输入数据可以合理划分为若干个等价类，在每一个等价类中取一个数据作为测试的输入条件，就可以用少量的代表性的测试数据取得比较好的效果。

等价类划分可以分为：

- 有效等价类：对于程序的规格说明来说是合理的，有意义的输入数据构成的集合，利用有效等价类可以检验程序是否实现了规格说明中所规定的功能和意义
- 无效等价类：与有效等价类相反，是指对程序的规格说明无意义，不合理的数据构成的集合

## 2.2 划分原则
- 如果规定了输入值的范围，可以定义一个有效等价类和两个无效等价类
- 如果规定了输入的规则，可以划分出一个有效的等价类（符合规则）和若干无效的等价类（从不同角度违反原则）
- 如果规定了输入数据的已组织，且程序对不同输入值做不同处理，则每个允许的输入值是一个有效等价类，并有一个无效等价类
- 如果规定了输入数据是整型，可以划分出正整数、零、负整数三个有效等价类 
- 处理表格时，有效类为空表、含一项的表、含多项的表等

# 3 边界值分析
## 3.1 边界值分析设计原则
边界值分析作为等价类划分的补充，通过选择等价类的边界值作为测试用例。

基于边界值分析有如下原则：

- 如果输入条件规定了值的范围，应选择刚到达这个范围的边界的值，以及刚刚超过这个范围边界的值作为测试输入数据
- 如果输入条件中规定了值的个数，则用最大个数、最小个数、比最小个数少一，比最大个数多一作为测试数据
- 如果规格说明书给出的输入域或输出域的有序集合，则应选取集合的第一个元素和最后一个元素作为测试用例
- 如果程序中使用了内部数据结构，则应选择内部数据结构的边界上的值作为测试用例

## 3.2 边界分析的两种方法
一般包括：

- 一般边界值分析：一般取`Min`、`Min+`、`Normal`、`Max-`、`Max`
- 健壮性边界值分析：除了一般边界值分析外，还包括`Min-`、`Max+`

# 4 决策表
决策表又叫判定表，是分析多种逻辑条件下执行不同操作的技术。决策表由四个部分组成，包括：

- 条件桩：列出问题的所有条件，条件的顺序无关紧要
- 动作桩：列出问题规定可能采取的所有动作，排列顺序没有约束 
- 条件项：列出了针对条件桩的取值在所有可能情况下的真假值
- 动作项：列出了在条件项的各种取值的有机关联情况下应采取的动作

另一方面，决策表中的规则，指的是任何条件组合的特定取值以及相应要执行的动作，在决策表中贯穿条件项和动作项的列就是规则，决策表中列出多少条件取值，就对应多少规则，条件项就有多少列。

比如下面是一个使用决策表制作的打印机测试用例：

![在这里插入图片描述](https://img-blog.csdnimg.cn/2021033109210066.png)

# 5 因果图
## 5.1 定义
因果图利用图解法分析输入的各种组合情况，适合于描述多种输入条件的组合，相应产生多个动作的方法，因果图的好处如下：

- 考虑多个输入之间的相互组合、相互制约的关系
- 指导测试用例的选择，指出需求规格说明描述中存在的问题 
- 能够帮助测试人员按照一定的步骤，高效率地开发测试用例
- 因果图法是一种严格地将自然语言规格说明转化为形式语言规格说明的方法，可以指出规格说明存在的不完整性和二义性

## 5.2 基本图形符号
原因结果图：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210331103407748.png)

`ci`和`ei`都可以取值0或1，0表示状态不出现，1表示状态出现。

约束图：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210331104503159.png)

# 6 场景法
同一事件不同的触发顺序和处理结果形成事件流，每个事件流触发时的情景便形成了场景。

场景法一般包含基本流和备选流（也叫备用流），从一个流程开始，通过描述经过的路径来确定过程，经过遍历所有的基本流和备用流来形成整个场景。场景法的基本设计步骤如下：

- 根据说明，描述程序的基本流以及各项备选流 
- 根据基本流和各项备选流生成不同的场景
- 对每一个场景生成相应的测试用例
- 对生成的所有测试用例重新复审，去掉多余的测试用例，测试用例确定后，对每一个测试用例确定测试数据值

图示如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210331110653842.png)





