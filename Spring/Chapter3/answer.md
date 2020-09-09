# 1 `Bean`实例化方法

三种：

- 构造方法实例化
- 静态工厂实例化
- 实例工厂实例化


# 2 注解装配的基本用法

- 首先使用`@Component`声明`Bean`
- 接着使用`@Autowired`/`@Resource`进行自动装配
- 最后在配置文件加上组件扫描`<context:component-scan base-package="xxx.xxx.xxx" />`

# 3 `@Autowired`与`@Resouce`的区别

## 所在包不同

`@Autowired`位于`org.springframework.beans.factory.annotation`下，而`@Resource`位于`javax.annotation`下，换句话说，前者是`Spring`的，后者是`JDK`的。

## 装配方式不同
`@Autowired`默认按照类型装配，而`@Resource`默认按照名称装配，比如下面这种情况既可以使用`@Autowired`，也可以使用`@Resource`：
```java
interface C{}

class A implements C{}

class Test
{
    @Autowired/@Resource
    private C a; 
}
```
但是如果还有另一个接口实现：
```java
interface C{}

@Component
class A implements C{}

@Component
class B implements C{}

class Test
{
    @Resource
    private C a; 
}
```
这样就不能使用`@Autowired`了，因为`@Autowired`按照类型装配，实现了`C`接口的有`A`与`B`这两个类，`Spring`不知道注入哪一个到`Test`中的`a`中，而如果使用`@Resource`，以为`@Resource`按照名称装配，这样的话类`A`的`bean`的`id`为`a`，正是私有成员的名字`a`，这样就相当于注入了`A`。
