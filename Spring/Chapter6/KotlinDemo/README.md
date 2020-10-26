# 关于`Gradle`的`Generator`插件

由于`MyBatis`官方没有推出`Gradle`的插件，只有`Maven`插件，因此这里用的是别人开源的轮子，[戳这里](https://plugins.gradle.org/plugin/org.hisoka.gradle.mybatis-generator-plugin)。

使用时加上该插件：

```bash
plugins {
  id "org.hisoka.gradle.mybatis-generator-plugin" version "0.0.1"
}
```

同时加上配置：

```bash
mybatisGenerator{
    configFile = "src/main/resources/generatorConfig.xml"
    configPropertiesFile = "src/main/resources/generatorConfig.xml"
    verbose = true
}
```
这里实际上是分为两个配置文件的，一个是`Generator`配置文件，一个是`MyBatis`配置文件，由于都写在一起了，所以就用同一个文件。

测试的时候发现该插件不需要手动加上其他依赖，也就是不需要手动加上`MyBaits`以及数据库驱动依赖。

写好配置文件后双击插件进行生成即可：

![](https://img-blog.csdnimg.cn/20200929195034834.png)

注意需要确保对应的包路径存在。

# 另外

另外笔者发现该插件最近的更新时间是2019年2月（本文写作时间2020/09/29），当初笔者也不知道其实还有更新的`Generator`插件，直接在`Gradle Plugin`[官网](https://plugins.gradle.org/)搜索即可：

![](https://img-blog.csdnimg.cn/20200929195124957.png)

基本上都是个人开发的插件，有一些是比较新的，建议使用更新时间比较接近的。


