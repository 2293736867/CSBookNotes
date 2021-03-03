# Table of Contents

* [1 来源](#1-来源)
* [2 `Java`里程碑](#2-java里程碑)
  * [2.1 `Java`起源](#21-java起源)
  * [2.2 `JDK 1.0`](#22-jdk-10)
  * [2.3 `JDK 1.2-1.7`](#23-jdk-12-17)
  * [2.4 `JDK 1.8+`](#24-jdk-18)
* [3 `JVM`种类简介](#3-jvm种类简介)
* [4 `JVM`简单编译调试实战](#4-jvm简单编译调试实战)
  * [4.1 获取源码+`BootJDK`](#41-获取源码bootjdk)
  * [4.2 安装依赖库](#42-安装依赖库)
  * [4.3 编译](#43-编译)
  * [4.4 调试](#44-调试)
  * [4.5 `JVM`下载](#45-jvm下载)


# 1 来源
- 来源：《Java虚拟机 JVM故障诊断与性能优化》——葛一鸣
- 章节：第一章

本文是第一章的一些笔记整理。

# 2 `Java`里程碑
## 2.1 `Java`起源
1990年`Sun`公司决定开发一门新的程序语言——`Oak`，已经具备安全性、网络通信、面向对象、垃圾回收、多线程等特性，由于`Oak`已被注册，于是改名为`Java`。

## 2.2 `JDK 1.0`
1995年`Sun`发布了`Java`以及`HotJava`产品，1996年正式发布`JDK 1.0`，包括两部分：

- 运行环境：`JRE`，包括核心`API`，用户界面`API`，发布技术、`JVM`等
- 开发环境：`JDK`，包括编译器`javac`等

1997年发布`JDK1.1`。

## 2.3 `JDK 1.2-1.7`
1998年发布`JDK 1.2`，`JDK1.2`兼容智能卡和小型消费类设备，还兼容大型服务器系统。同时`Sun`发布`JSP/Servlet`+`EJB`规范，将`Java`分成了`J2EE`、`J2SE`、`J2ME`。

2000年发布`JDK 1.3`，默认虚拟机改为`Hotspot`。

2002年发布`JDK1.4`，`Classic`虚拟机退出舞台。

2004年发布`JDK 1.5`，支持泛型、注解、自动装箱拆箱、枚举、可变长参数等。

2006年发布`JDK 1.6`，`Java`开源并建立了`OpenJDK`。

2011年发布`JDK 1.7`，启用了`G1`垃圾回收器，支持64位系统的压缩指针以及`NIO 2.0`。

## 2.4 `JDK 1.8+`
2014年发布`JDK 1.8`，`JDK 1.8`是一个`LTS`版，到目前还支持，引入了全新的`Lambda`。

2017年发布`JDK 9`。

2018年发布`JDK 10`。

2018年发布`JDK 11`，又一个`LTS`版，引入了字符串增强、`Epsilon`垃圾收集器、`ZGC`等。

# 3 `JVM`种类简介
`Java`发展初期，使用的是`Classic`虚拟机，之后在`Solaris`短暂地使用过`Exact VM`虚拟机，到现在被大规模部署和使用的是`Hotspot`虚拟机。

另外，在`IBM`内部使用着一款叫`J9`的虚拟机，`Apache`也曾经推出过`Apache Harmony`，基于`JDK 5`以及`JDK 6`，于2011年停止开发。

# 4 `JVM`简单编译调试实战
下面以`OpenJDK15`为例，对`OpenJDK 15 JVM`进行源码编译。

（注：由于笔者系统为`Manjaro`，这是一个滚更的系统，很多工具链都会更新到最新的状态，比如`GCC 10.2`，书籍中的例子是利用`JDK8`去编译`JDK10`，实际测试发现会报错，`configure`成功了但是`make`失败，然后就切换到最新的`JDK`，就编译成功了。对于不是滚更的系统，可以使用`JDK10`去编译`JDK11`等，而非采用目前最新的`JDK15`）

## 4.1 获取源码+`BootJDK`
戳[这里](https://jdk.java.net/java-se-ri/15)下载：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210301194640139.png)

可以使用如下命令检测下载文件的完整性：
```bash
echo "bb67cadee687d7b486583d03c9850342afea4593be4f436044d785fba9508fb7 openjdk-15+36_linux-x64_bin.tar.gz" | sha256sum --check
echo "d07bf62b4b20fa6bcd4c8fcd635e5df20b7c090af291675b2bd99f8cea8760a0 openjdk-15+36_src.zip" | sha256sum --check
```

另外需要准备一个`BootJDK`，根据`BootJDK`的规则：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210301185154595.png)

建议使用`当前版本号`/`版本号-1`/`版本号-2`的`JDK`，这里选用的是`OpenJDK 15`。

## 4.2 安装依赖库
笔者系统`Manajro`，需要安装一些基础依赖：
```bash
paru -S base-devel
# 或
pacman -S base-devel
# 或
yay -S base-devel
```
如果依赖库安装不完整在配置阶段以及编译阶段会给出相应提示，再进行对应依赖安装即可。

## 4.3 编译
解压源码进入目录：
```bash
unzip openjdk-15+36_src.zip
tar -zxvf openjdk-15+36_linux-x64_bin.tar.gz
cd openjdk
```
配置：
```bash
bash configure --with-debug-level=slowdebug --with-jvm-variants=server --with-target-bits=64 --with-memory-size=8000 --disable-warnings-as-errors --with-native-debug-symbols=internal --with-boot-jdk=../jdk-15
```

参数说明：

- `--with-debug-level=slowdebug`：编译`DEBUG`版本的`JDK`，选项可以是`slowdebug`/`fastdebug`/`release`/`optimized`
- `--with-jvm-variants=server`：构建`server`变体的`Hotspot`，选项可以是`server`/`client`/`minimal`/`core`/`zero`/`custom`
- `--with-target-bits=64`：编译64位的`JDK`，编译32位可以使用`--with-target-bits=32`
- `--with-memory-size=8000`：编译的计算机至少需要8G内存，这个可以根据个人需要调整
- `--disable-warnings-as-errors`：忽略警告的信息，注意该参数很重要，不加的话会显示配置成功但`make`失败
- `--with-native-debug-symbols=internal`：生成`symbol`文件，便于后续调试，选项可以是`internal`/`none`/`external`/`zipped`
- `--with-boot-jdk`：`BootJDK`的目录

结果：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210301221634945.png)

配置后进行编译：
```bash
make images
```
这个阶段需要一点时间，而且会把`CPU`拉满，好了之后会提示`Finished building`：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210301221606736.png)

笔者环境下编译出来的`JDK`占了3G：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210301224350705.png)

进入对应目录可以查看版本：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210301224537520.png)

## 4.4 调试
调试需要`gdb`，先安装好`gdb`：
```bash
paru -S gdb
```
进入`bin`目录（`build/linux-x86_64-server-slowdebug/jdk/bin`），输入：
```bash
gdb -args ./java -version
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210302103935814.png)

在`main`函数打断点：
```bash
(gdb) b main
```
再执行`run`，可以看到会停在`java.base/share/native/launcher/main.c`第98行：

```bash
(gdb) run
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210302104117877.png)

再次输入`n`可进行单步调试：
```bash
(gdb) n
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210302104249857.png)

这样就算完成了基础的调试操作，为进一步学习`JVM`准备好基本的环境。

## 4.5 `JVM`下载
如果编译失败的话，笔者这里提供了自己编译出来的`JVM`：

- [Github](https://github.com/2293736867/OpenJDK15FromSourceCode)
