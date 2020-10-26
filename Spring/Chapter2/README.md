# 1 `IoC`与`DI`
`IoC`是`Inversion of Control`的简称，也就是控制反转。通常来说，创建对象需要调用者手动创建，也就是`new XXX()`的方式。当`Spring`框架出现后，对象的实例不再由调用者创建，而是由`Spring`容器创建，这样控制权就由调用者转移到`Spring`容器，控制权发生了反转，这就是`Spring`的控制反转。从`Spring`容器来看，`Spring`容器负责将被依赖对象赋值给调用者的成员变量，相当于为调用者注入它所依赖的实例，这就是`Spring`的依赖注入（`Dependency Injection`，`DI`）。

一句话总结：

- **`IoC`：控制权由调用者交由`Spring`容器，控制发生了反转**
- **`DI`：由`Spring`容器注入需要的值到对象中**

# 2 `Spring IoC`容器
`Spring`中实现`IoC`的是`Spring IoC`容器，主要基于以下两个接口：

- `BeanFactory`
- `ApplicationContext`

## 2.1 `BeanFactory`
位于`org.springframework.beans.factory`下，提供了完整的`IoC`服务支持，是一个管理`Bean`工厂，主要负责初始化各种`Bean`。可以通过`XmlBeanFactory`来获取`XML`文件中的`Bean`并进行装配，例子如下：

```java
BeanFactory factory = new XmlBeanFactory(new FileSystemResource("/xxx/xxx/xxx/xxx/applicationContext.xml"));
TestInterface test = (TestInterface)factory.getBean("test");
test.hello();
```
需要使用绝对路径，而且，该方法已经过时了：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200908123818784.png)

因此不推荐使用。


## 2.2 `ApplicationContext`
`ApplicationContext`是`BeanFactory`的子接口，也称为应用上下文，除了包含`BeanFactory`的功能外还添加了国际化、资源访问、事件传播等的支持，创建`ApplicationContext`的实例有以下三种方法：

- `ClassPathXmlApplicationContext`
- `FileSystemXmlApplicationContext`
- `Web`服务器实例化

### 2.2.1 `ClassPathXmlApplicationContext`
该类从`resources`下寻找指定的`XML`文件：
```java
ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
TestInterface test = (TestInterface)context.getBean("test");
test.hello();
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/202009081242295.png)

### 2.2.2 `FileSystemXmlApplicationContext`
该类读取配置文件需要加上前缀：

- `classpath:`：该前缀表示从类路径读取，对于`Maven`项目来说就是`resources`
- `file:`：该前缀表示从绝对路径获取

例子：

```java
ApplicationContext context = new FileSystemXmlApplicationContext("classpath:applicationContext.xml");
//ApplicationContext context = new FileSystemXmlApplicationContext("file:/xxx/xxx/xxx/xxxx/xxx/applicationContext.xml");
```

### 2.2.3 `Web`服务器实例化
一般使用基于`ContextLoaderListener`的实现方式，修改`web.xml`，添加如下代码：

```xml
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:applicationContext.xml</param-value>
    <listener>
        <listener-class>
            org.springframework.web.context.ContextLoaderListener
        </listener-class>
    </listener>
</context-param>
```


# 3 `DI`的两种方法
`DI`通常有两种实现方式：

- 构造方法注入
- `setter`注入

下面分别来看一下。

## 3.1 构造方法注入
`Spring`可以利用反射机制通过构造方法完成注入，比如有以下三个类：
```java
public interface TestInterface {
    void hello();
}

public class TestA implements TestInterface {
    @Override
    public void hello() {
        System.out.println("Test A");
    }
}

public class TestB {
    private TestInterface test;

    public TestB(TestInterface test)
    {
        this.test = test;
    }

    public void method()
    {
        test.hello();
    }
}
```
`TestInterface`是一个简单的接口，而`TestA`实现了该接口，`TestB`需要一个`TestInterface`类型的对象，因此可以先注入一个`TestA`，再将该`TestA`注入到`TestB`的构造方法中：

```xml
<bean id="testA" class="TestA"/> <!--注入一个TestA对象-->
<bean id="testB" class="TestB">
	<constructor-arg index="0" ref="testA" /> <!--将上面注入的TestA作为参数传入构造方法中，在传给TestB的私有成员-->
</bean>
```
`constructor-arg`是用于定义通过构造方法的方式进行注入的标签，`index`定义位置，从`0`开始，`ref`是某个`Bean`的引用，值为该`Bean`的`id`。

## 3.2 通过`setter`注入
在上面的例子中，修改`TestB`如下：
```java
public class TestB {
    private TestInterface test;

    public void setTest(TestInterface test) {
        this.test = test;
    }

    public void method()
    {
        test.hello();
    }
}
```
其实就是添加了一个`setter`，接着修改配置文件：

```xml
<bean id="testA" class="TestA"/>
<bean id="testB" class="TestB">
    <property name="test" ref="testA" />
</bean>
```
`<property>`表示通过`setter`注入，`name`是私有成员的名字，`ref`是被传入`setter`的`Bean`的`id`值。
