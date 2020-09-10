# 目录
- [1](https://github.com/2293736867/CSBookNotes/edit/master/Spring/Chapter1/README.md/#4) 

# 1 `Spring`简介
`Spring`是一个轻量级`Java`开发框架，最早由`Rod Johnson`创建，目的是为了解决企业级应用开发的业务逻辑层和其他各层的耦合问题，是一个分层的`Java SE/EE full-stack`轻量级开源框架，为开发`Java`应用程序提供全面的基础架构支持。

# 2 `Spring`体系结构
目前`Spring`已经集成了20多个模块，分布在以下模块中：

- `Core Container`：核心容器
- `Data Access/Integration`：数据访问/集成
- `Web`：`Web`模块
- `AOP`：`Aspect Oriented Programming`，面向切面编程
- `Instrumentation`：植入模块
- `Messaging`：消息传输
- `Test`：测试模块

如图所示：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200907230724234.png)

## 2.1 核心容器
由`Spring-core`、`Spring-beans`、`Spring-context`、`Spring-context-support`、`Spring-expression`等模块组成：

- `Spring-core`：提供了框架的基本组成部分，包括`Ioc`（`Inversion of Control`，控制反转）以及`DI`（`Dependency Injection`，依赖注入）功能
- `Spring-beans`：提供了`BeanFactory`，是工厂模式的一个典型实现，**`Spring`将管理的对象称为`Bean`**
- `Spring-context`：建立在`Core`和`Beans`模块的基础上，提供了一个框架式的对象访问方式，是访问定义和配置的任何对象的媒介，`ApplicationContext`接口是`Context`模块的焦点
- `Spring-context-support`：支持整合第三方库到`Spring`应用程序上下文，特别是用于高速缓存（`EhCache`、`JCache`）和任务调度（`CommonJ`、`Quartz`）的支持
- `Spring-expression`：提供了强大的表达式语言去支持运行时查询和操作对象图，是`JSP 2.1`规定的统一表达式语言的扩展

## 2.2 `AOP`和`Instrumentation`
- `Spring-aop`：提供了一个符合`AOP`要求的面向切面的编程实现，允许定义方法拦截器和切入点
- `Spring-aspects`：提供了与`AspectJ`的集成功能，`AspectJ`是一个功能强大且成熟的`AOP`框架
- `Spring-instrumentation`：提供了类植入支持和类加载器的实现

## 2.3 消息
`Spring 4.0`后增加了消息模块，提供了对消息传递体系结构和协议的支持。

## 2.4 数据访问/集成
数据访问/集成层由`JDBC`、`ORM`、`OXM`、`JMS`和事务模块组成。

- `Spring-JDBC`：提供了一个`JDBC`抽象层，消除了繁琐的`JDBC`编码和数据库厂商特有的错误代码解析
- `Spring-ORM`：为流行的`ORM`（`Object-Relational Mapping`，对象关系映射）框架提供了支持，包括`JPA`和`Hibernate`，使用`Spring-ORM`框架可以将这些`O/R`映射框架与`Spring`提供的所有其他功能结合使用
- `Spring-OXM`：提供了一个支持对象/`XML`映射的抽象层实现，例如`JAXB`、`Castor`、`JiBX`、`XStream`
- `Spring-JMS`：`JMS`（`Java Messaging Service`，`Java`消息传递服务），包含用于生产和使用消息的功能
- `Spring-TX`：事务管理模块，支持用于实现特殊接口和所有`POJO`类的编程和声明式事务管理


## 2.5 `Web`
`Web`有`Spring-Web`、`Spring-WebMVC`、`Spring-WebSocket`和`Portlet`模块组成。

- `Spring-Web`：提供了基本的`Web`开发集成功能，例如多文件上传等
- `Spring-WebMVC`：也叫`Web-Servlet`模块，包含用于`Web`应用程序的`Spring MVC`和`REST Web Services`的实现。
- `Spring-WebSocket`：提供了`WebSocket`和`SockJS`的实现
- `Porlet`：类似于`Servlet`模块的功能，提供了`Porlet`环境下`MVC`的实现

## 2.6 测试
`Spring-test`模块支持使用`JUnit`或`TestNG`对`Spring`组件进行单元测试和集成测试。

# 3 环境
- `OpenJDK 11.0.8`
- `IDEA 2020.2`
- `Maven`/`Gradle`

# 4 入门`Demo`（`Java`版）
## 4.1 新建`Maven`工程
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200907233051206.png)

![在这里插入图片描述](https://img-blog.csdnimg.cn/2020090723311784.png)

## 4.2 引入依赖
`pom.xml`文件加入：
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>5.2.8.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-core</artifactId>
        <version>5.2.8.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-beans</artifactId>
        <version>5.2.8.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-web</artifactId>
        <version>5.2.8.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-test</artifactId>
        <version>5.2.8.RELEASE</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>commons-logging</groupId>
        <artifactId>commons-logging</artifactId>
        <version>1.2</version>
    </dependency>
	<dependency>
	    <groupId>org.junit.jupiter</groupId>
	    <artifactId>junit-jupiter-api</artifactId>
	    <version>5.6.2</version>
	    <scope>test</scope>
	</dependency>
</dependencies>
```
可以[戳这里](https://mvnrepository.com/)查看最新的版本。


## 4.3 新建文件
新建如下5个空文件：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200907235716608.png)


## 4.4 `applicationContext.xml`
该文件是`Spring`的配置文件，习惯命名为`applicationContext.xml`，内容如下：

```xml
<?xml version="1.0" encoding="utf-8" ?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="test" class="TestImpl"/>
</beans>
```
这里声明了一个`id`为`test`，类为`TestImpl`的`Bean`。

## 4.5 `TestInterface`
```java
public interface TestInterface {
    void hello();
}

```

## 4.6 `TestImpl`
```java
public class TestImpl implements TestInterface {
    @Override
    public void hello() {
        System.out.println("Hello Spring.");
    }
}
```

## 4.7 `Main`
```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello");
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        ((TestInterface)context.getBean("test")).hello();
    }
}
```

## 4.8 `Tests`
```java
public class Tests {
    @Test
    public void test()
    {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        TestInterface testInterface = (TestInterface)context.getBean("test");
        testInterface.hello();
    }
}
```


## 4.9 运行
### 4.9.1 测试
直接点击测试类旁边的按钮即可：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200907234955345.png)

若出现如下错误：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200907234516940.png)

是`JDK`版本设置错误的问题，先打开`Project Structure`，修改`Modules`下的`JDK`版本：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200907234551832.png)

下一步是打开设置，修改`Comiler`下的`JDK`版本：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200907234617820.png)

输出：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200907235111902.png)

### 4.9.2 `Main`
默认不能直接运行`Main`函数，需要添加运行配置：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200907235151867.png)

选择`Application`添加配置，并且指定`Name`以及`Main class`：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200907235315411.png)

这样就可以运行了：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200907235415796.png)

# 5 `Kotlin`版`Demo`
使用`Gradle`+`Kotlin`的入门`Demo`。

## 5.1 新建`Gradle`工程

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200908000719552.png)


![在这里插入图片描述](https://img-blog.csdnimg.cn/20200907235621499.png)

## 5.2 `build.gradle`
完整文件如下：
```bash
plugins {
    id 'java'
    id 'org.jetbrains.kotlin.jvm' version '1.4.0'
}

group 'org.example'
version '1.0-SNAPSHOT'

repositories {
    mavenCentral()
}

dependencies {
    compile group: 'org.springframework', name: 'spring-context', version: '5.2.8.RELEASE'
    compile group: 'org.springframework', name: 'spring-core', version: '5.2.8.RELEASE'
    compile group: 'org.springframework', name: 'spring-beans', version: '5.2.8.RELEASE'
    testCompile group: 'junit', name: 'junit', version: '4.12'
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
}
compileKotlin {
    kotlinOptions {
        jvmTarget = "11"
    }
}
```
除了添加依赖以外还添加了一些其他参数。

## 5.3 新建文件夹以及文件

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200908003800783.png)



## 5.4 `TestInterface`
```kotlin
interface TestInterface {
    fun hello()
}
```

## 5.5 `TestImpl`
```kotlin
class TestImpl:TestInterface
{
    override fun hello() {
        println("Hello Spring")
    }
}
```

## 5.6 `Main`
```kotlin
fun main() {
    println("Hello")
    val context: ApplicationContext = ClassPathXmlApplicationContext("applicationContext.xml")
    val test: TestInterface = context.getBean("test") as TestInterface
    test.hello()
}
```


## 5.7 `applicationContext.xml`
同上。

## 5.8 `Tests`
```kotlin
class Tests {
    @Test
    fun test()
    {
        val context:ApplicationContext = ClassPathXmlApplicationContext("applicationContext.xml")
        val test:TestInterface = context.getBean("test") as TestInterface
        test.hello()
    }
}
```

## 5.9 运行
### 5.9.1 测试
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200908003226273.png)

同样直接点击旁边的按钮即可运行：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200908003258540.png)

### 5.9.2 `Main`
同样点击按钮即可：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200908003338106.png)

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200908003412136.png)

# 6 源码

- [Github](https://github.com/2293736867/CSBookNotes/tree/master/Spring/Chapter1/JavaSpringDemo)
- [码云](https://gitee.com/u6b7b5fc3/CSBookNotes/tree/master/Spring/Chapter1/JavaSpringDemo)
