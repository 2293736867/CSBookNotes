# 1 概述
本文主要讲述了`AOP`的基本概念以及在`Spring`中`AOP`的几种实现方式。

# 2 `AOP`
`AOP`，即`Aspect-Oriented Programming`，面向切面编程，与`OOP`相辅相成。类似的，在`OOP`中，以类为程序的基本单元，在`AOP`中的基本单元是`Aspect`（切面）。`AOP`采用横向抽取的机制，将分散在各个方法中重复的代码提取出来，然后在程序编译或运行阶段将这些抽取出来的代码应用到需要执行的地方，这种横向抽取机制是`OOP`无法办到的。

`AOP`最典型的一个应用就是抽离出业务模块中与业务不相关的代码，比如，日志记录，性能统计，安全控制，事务处理等。假设业务代码如下：

```java
public void save()
{
	saveUser();
}
```
但是随着软件开发越来越复杂，业务代码变成了下面的样子：
```java
public void save()
{
	//安全控制
	//性能统计
	//事务处理
	//日志记录
	saveUser();
}
```
这样业务类就回混杂很多业务无关的代码，不仅会显得类臃肿不堪，同时也难于开发人员进行维护。因此，引入`AOP`后，可以将安全控制等的代码抽取出来，交给`AOP`机制在编译时或运行时再将这些代码进行动态“织入”，这样就可以让业务层专注于业务代码，而不用混杂其他逻辑的代码。

# 3 `AOP`实现方式
`AOP`的实现方式主要有以下几种：

- 动态代理：`JDK`、`CGLIB`、`Javassist`、`ASM`等
- `Spring`中`AOP`实现：基于代理类/`AspectJ`实现

下面先来看一个简单的例子。

# 4 动态代理实现
`Java`中有多种动态代理技术，比如：

- `JDK`
- `CGLIB`
- `Javassist`
- `ASM`

等等，在`Spring AOP`中常用的有`JDK`和`CGLIB`，先看一下最经典的`JDK`动态代理的实现。

## 4.1 `JDK`实现
结构：

- `JDKInterface`：测试接口，含三个方法
- `JDKImpl`：测试接口实现类
- `JDKAspect`：切面类
- `JDKProxy`：代理类
- `JDKTest`：测试类


首先对接口类进行编写，包含三个方法：
```java
public interface JDKInterface {
    void save();
    void modify();
    void delete();
}
```
编写实现类：
```java
public class JDKImpl implements JDKInterface {
    @Override
    public void save() {
        System.out.println("保存");
    }

    @Override
    public void modify() {
        System.out.println("修改");
    }

    @Override
    public void delete() {
        System.out.println("删除");
    }
}
```
接下来是定义切面类，模拟上面提到的异常处理等功能：
```java
public class JDKAspect {
    public void check()
    {
        System.out.println("模拟权限控制");
    }

    public void except()
    {
        System.out.println("模拟异常处理");
    }

    public void log()
    {
        System.out.println("模拟日志记录");
    }

    public void monitor()
    {
        System.out.println("模拟性能监测");
    }
}

```
再接着是很关键的代理类，该类的任务是创建代理对象，并且负责如何对被代理对象进行增强处理：
```java
public class JDKProxy implements InvocationHandler {
    public JDKInterface testInterface;
    public Object createProxy(JDKInterface testInterface)
    {
        this.testInterface = testInterface;
        //获取代理类的类加载器
        ClassLoader loader = JDKProxy.class.getClassLoader();
        //获取被代理对象（目标对象）的接口
        Class<?>[] classes = testInterface.getClass().getInterfaces();
        //创建代理后的对象
        return Proxy.newProxyInstance(loader,classes,this);
    }
    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
    	//创建一个切面
        JDKAspect aspect = new JDKAspect();
        //前增强
        aspect.check();
        aspect.except();
        //调用被代理对象的方法，这里指的是testInterface的方法，objects是参数，obj是返回值
        Object obj = method.invoke(testInterface,objects);
        //后增强
        aspect.log();
        aspect.monitor();
        return obj;
    }
}
```
创建代理对象时，需要指定代理类的类加载器以及被代理对象的接口，接着通过`Proxy.newProxyInstance`创建代理对象。

测试：
```java
public class JDKTest {
    public static void test()
    {
        JDKProxy proxy = new JDKProxy();
        JDKInterface testInterface = (JDKInterface) proxy.createProxy(new JDKImpl());
        testInterface.save();
        testInterface.modify();
        testInterface.delete();
    }
}
```
输出：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200915172558491.png)

可以看到：

- 在执行真正的业务操作之前，被动态织入了权限控制，异常处理功能
- 在执行业务操作后被织入了日志记录和性能监测功能

这就是`AOP`的一种最典型的应用。


## 4.2 `CGLIB`实现
首先添加依赖（由于这是新建的工程需要手动添加`CGLIB`，在`Spring Core`中已经包含了`CGLIB`无需手动添加）：

```xml
<dependency>
    <groupId>cglib</groupId>
    <artifactId>cglib</artifactId>
    <version>3.3.0</version>
</dependency>
```

结构与上面的类似：

- `CGLibInterface`：一个简单的接口
- `CGLibImpl`：接口实现类
- `CGLibProxy`：代理对象创建类
- `CGLibAspect`：切面类
- `CGLibTest`：测试类

首先是接口类和接口实现类，与上面的一样：
```java
public interface CGLibTestInterface {
    void save();
    void modify();
    void delete();
}

public class CGLibImpl implements CGLibTestInterface{
    @Override
    public void save() {
        System.out.println("保存");
    }

    @Override
    public void modify() {
        System.out.println("修改");
    }

    @Override
    public void delete() {
        System.out.println("删除");
    }
}
```
切面类也一样：
```java
public class CGLibAspect {
    public void check()
    {
        System.out.println("模拟权限控制");
    }

    public void except()
    {
        System.out.println("模拟异常处理");
    }

    public void log()
    {
        System.out.println("模拟日志记录");
    }

    public void monitor()
    {
        System.out.println("模拟性能监测");
    }
}
```
唯一不同的是代理类，需要实现`MethodInterceptor`接口：
```java
public class CGLibProxy implements MethodInterceptor {
    public Object createProxy(Object target)
    {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target.getClass());
        enhancer.setCallback(this);
        return enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        CGLibAspect aspect = new CGLibAspect();
        aspect.check();
        aspect.except();
        Object obj = methodProxy.invokeSuper(o,objects);
        aspect.log();
        aspect.monitor();
        return obj;
    }
}
```
其中代理对象创建过程如下：
```java
Enhancer enhancer = new Enhancer();
enhancer.setSuperclass(target.getClass());
enhancer.setCallback(this);
return enhancer.create();
```
首先使用`Enhancer`生成一个代理类，该代理类的父类通过`setSuperclass`设置，回调函数通过`setCallback`设置，其中`setCallback`的参数为`MethodInterceptor`，也就是实现了该接口的类，最后通过`create`创建代理对象。

接着在`intercept`对方法进行织入，过程与`JDK`的实现类似。

测试：
```java
public class CGLibTest {
    public static void test() {
        CGLibProxy proxy = new CGLibProxy();
        CGLibTestInterface cgLibTestInterface = (CGLibTestInterface) proxy.createProxy(new CGLibImpl());
        cgLibTestInterface.delete();
        cgLibTestInterface.modify();
        cgLibTestInterface.save();
    }
}
```
输出也是一致：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200915175316334.png)的

这里因为笔者用的是`OpenJDK11`，使用`CGLIB`包时会有非法反射访问警告：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200915175746580.png)

加上`JVM`参数就好了：

```bash
--illegal-access=deny --add-opens java.base/java.lang=ALL-UNNAMED
```


# 5 相关术语
看了两个`Demo`后应该可以大概知道`AOP`的作用了，下面正式介绍相关术语：

- **`Join Pont`：`连接点`，可能需要注入`切面`的地方，包括方法调用的前后等等**
- **`Pointcut`：`切点`，需要特殊处理的`连接点`，通过`切点`确定哪些`连接点`需要处理，比如上面的两个接口`JDKInterface`和**`CGLibInterface`中的方法就是`切点`，当调用这些接口中的方法（也就是`切点`）时需要进行特殊的处理
- **`Advice`：`通知`，定义在什么时候做什么事情，上面的例子没有提到，下面`Spring AOP`的例子会有体现**
- **`Aspect`：`切面`，`通知+切点`的集合，类似于`Java`中的类声明，比如上面的`JDKAspect`以及`CGLibAspect`就相当于`切面`**
- **`Target`：`目标对象`，需要被通知的对象，比如上面的`new CGLibImpl()`以及`new JDKImpl()`，也就是实现了接口的对象**
- **`Proxy`：`代理`，通知应用到`目标对象`后被创建的对象，比如上面的通过`createProxy`创建的对象**
- **`Weaving`：`织入`，把`切面`代码插入到`目标对象`后生成`代理`对象的过程，比如上面的把`check`、`except`等代码插入到`目标对象`（`new CGLibImpl()`以及`new JDKImpl()`）后，生成代理对象（通过`Proxy.newInsatnce()`或`enhancer.create()`）的过程。`织入`有三种方式，一种是`编译器织入`，需要有特殊的编译器，一种是`类装载期织入`，需要有特殊的类装载器，一种是`动态代理织入`，在运行时期为目标类添加通知生成子类的方式，`Spring AOP`默认使用`动态代理织入`，`AspectJ`采用`编译器织入`和`类装载期织入`**


如果还是不明白就自己动手多实现几次，`AOP`确实是有点难理解，笔者也只能帮大家到这了。

# 6 `Spring AOP`实现
`Spring`中`AOP`实现主要有两种方式：

- 使用`JDK`自带的动态代理，配合`ProxyFactoryBean`实现
- 使用`AspectJ`实现，可以通过`XML`或注解配置实现

先来看一下代理类实现的方式。

## 6.1 代理类实现
使用代理类实现需要`ProxyFactoryBean`类，同时也需要了解一下`AOP`中的通知类型。

### 6.1.1 `Spring AOP`通知类型
6种：

- 环绕通知：目标方法执行前和执行后实施的增强
- 前置通知：目标方法执行前实施增强
- 后置返回通知：目标方法执行成功后实施增强，发生异常不执行
- 后置最终通知：目标方法执行后实施增强，不管是否发生异常都要执行
- 异常通知：抛出异常后实施增强
- 引入通知：在目标类添加一些新的方法和属性，可用于修改目标类

流程如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200915185146968.png)

### 6.1.2 `ProxyFactoryBean`
`ProxyFactoryBean`是`FactoryBean`的一个实现类，负责为其他`Bean`实例创建代理实例，`XML`配置中常用属性如下：

- `target`：代理的目标对象
- `proxyInterfaces`：代理需要实现的接口列表
- `interceptorNames`：需要织入目标的`Advice`
- `proxyTargetClass`：是否对类代理而不是接口，默认为`false`，使用`JDK`动态代理，为`true`时使用`CGLIB`
- `singleton`：代理类是否为单例，默认`true`
- `optimize`：设置为`true`时强制使用`CGLIB`

### 6.1.3 实现
依赖：
```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-aop</artifactId>
    <version>5.2.8.RELEASE</version>
</dependency>
```
结构类似：

- `ProxyClassInterface`：一个简单的接口
- `ProxyClassImpl`：接口实现类
- `ProxyClassAspect`：切面类
- `ProxyTest`：测试类

其中接口类与接口实现类与上面一致：
```java
public interface ProxyInterface {
    void save();
    void modify();
    void delete();
}

public class ProxyClassImpl implements ProxyInterface{
    @Override
    public void save() {
        System.out.println("保存");
    }

    @Override
    public void modify() {
        System.out.println("修改");
    }

    @Override
    public void delete() {
        System.out.println("删除");
    }
}
```
切面类实现了`MethodInterceptor`（`org.aopalliance.intercept.MethodInterceptor`），同时指定了增强方法：
```java
public class ProxyClassAspect implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        check();
        expect();
        Object obj = methodInvocation.proceed();
        log();
        monitor();
        return obj;
    }

    private void check()
    {
        System.out.println("模拟权限控制");
    }

    private void expect()
    {
        System.out.println("模拟异常处理");
    }

    private void log()
    {
        System.out.println("模拟日志记录");
    }

    private void monitor()
    {
        System.out.println("性能监测");
    }
}
```
这里调用目标方法相比起`CGLIB`以及`JDK`简单了很多，无需参数，返回`Object`，接着是测试类：
```java
public class ProxyClassTest {
    public static void test()
    {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        ProxyInterface proxyInterface = (ProxyInterface)context.getBean("factory");
        proxyInterface.delete();
        proxyInterface.save();
        proxyInterface.modify();
    }
}
```
配置文件：
```xml
<?xml version="1.0" encoding="utf-8" ?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="impl" class="com.proxyclass.ProxyClassImpl"/>
    <bean id="aspect" class="com.proxyclass.ProxyClassAspect" />
    <bean id="factory" class="org.springframework.aop.framework.ProxyFactoryBean">
        <property name="proxyInterfaces" value="com.proxyclass.ProxyInterface"/>
        <property name="target" ref="impl" />
        <property name="interceptorNames" value="aspect"/>
        <property name="proxyTargetClass" value="true"/>
    </bean>
</beans>
```
配置文件说明：

- `impl`：接口实现类`Bean`
- `aspect`：切面类`Bean`
- `factory`：代理`Bean`，`proxyInterfaces`指定代理实现的接口，`target`指定目标对象，`interceptorNames`指定切面，`proxyTargetClass`设置为`true`，指定使用`CGLIB`

输出：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200915190943743.png)

## 6.2 `XML`配置的`AspectJ`实现
### 6.2.1 `AspectJ`
`AspectJ`是一个基于`Java`的`AOP`框架，使用`AspectJ`实现`Spring AOP`的方法有两种，一种是基于`XML`配置，一种是基于注解配置，先来看基于`XML`配置的实现。

### 6.2.2 `XML`元素
基于`XML`配置的`AspectJ`需要在其中定义切面，切点以及通知，需要定义在`<aop:config>`内。

`<aop:config>`是`AspectJ`顶层配置元素，子元素如下：

- `<aop:aspect>`：定义切面
- `<aop:pointcut>`：定义切点
- `<aop:before>`：定义前置通知
- `<aop:after-returning>`：定义后置返回通知
- `<aop:around>`：定义环绕通知
- `<aop:after-throwing>`：定义异常通知
- `<aop:after>`：定义后置最终通知


### 6.2.3 实现
依赖：
```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-aspects</artifactId>
    <version>5.2.8.RELEASE</version>
</dependency>
```

接口与实现类：
```java
public interface XMLInterface {
    void save();
    void modify();
    void delete();
}

public class XMLImpl implements XMLInterface {
    @Override
    public void save() {
        System.out.println("保存");
    }

    @Override
    public void modify() {
        System.out.println("修改");
    }

    @Override
    public void delete() {
        System.out.println("删除");
    }
}
```
切面类：
```java
public class XMLAspect {
    public void before()
    {
        System.out.println("前置通知");
    }

    public void afterReturning()
    {
        System.out.println("后置返回通知");
    }

    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        System.out.println("环绕通知开始");
        Object object = proceedingJoinPoint.proceed();
        System.out.println("环绕通知结束");
        return object;
    }

    public void expect(Throwable e)
    {
        System.out.println("异常通知");
    }

    public void after()
    {
        System.out.println("后置最终通知");
    }
}
```
提供了更加精细的方法，比如前置通知以及后置通知。在环绕通知中，简化了目标方法的调用，只需要通过`proceed`调用即可获取返回值，测试类如下：

```java
public class XMLTest {
    public static void test()
    {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        XMLInterface xmlInterface = (XMLInterface)context.getBean("xmlImpl");
        xmlInterface.delete();
        xmlInterface.modify();
        xmlInterface.save();
    }
}
```
配置文件：
```xml
<bean id="xmlImpl" class="com.aspectj_xml.XMLImpl"/>
<bean id="xmlAspect" class="com.aspectj_xml.XMLAspect" />
<aop:config>
    <aop:pointcut id="pointCut" expression="execution(* com.aspectj_xml.XMLInterface.*(..))"/>
    <aop:aspect ref="xmlAspect">
        <aop:before method="before" pointcut-ref="pointCut"  />
        <aop:after-returning method="afterReturning" pointcut-ref="pointCut" />
        <aop:around method="around" pointcut-ref="pointCut" />
        <aop:after-throwing method="expect" pointcut-ref="pointCut" throwing="e"/>
        <aop:after method="after" pointcut-ref="pointCut"/>
    </aop:aspect>
</aop:config>
```
说明如下：

- `pointCut`定义了一个切点，`expression`是切点表达式
- `expression`中第一个`*`表示返回类型，使用`*`表示任意类型
- 注意第一个`*`与后面的包名有空格
- 第二个`*`表示该接口/类中的所有方法
- `(..)`表示方法的参数，`..`表示任意参数

关于`expression`更详细的例子请查看文档，[戳这里](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html#aop-api)：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200916002529989.png)

- `<aop:aspect>`定义了一个切面，里面包含了前置通知/后置通知/异常通知等等，`method`表示的是调用的方法，`pointcut-ref`是切点的引用，表示在哪一个切点上进行增强处理

### 6.2.4 输出
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200916002830767.png)

可以看到执行顺序为：
```bash
前置通知->环绕通知开始->目标方法->后置最终通知->环绕通知结束->后置返回通知
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200916002954562.png)

可以看到顺序为：
```bash
前置通知->环绕通知开始->目标方法-后置最终通知->异常通知
```
与上面的执行流程图一致。


## 6.3 注解配置的`AspectJ`实现（推荐）
### 6.3.1 常用注解
基于注解开发是目前最常用的方式，比`XML`要便捷很多，常见的注解如下：

- `@Aspect`：定义一个切面，注解在切面类上
- `@Pointcut`：定义切点表达式，需要一个返回值与方法体都为空的方法，并将`@Pointcut`注解在该空方法上
- `@Before`：定义前置通知，值可以是切点或切点表达式
- `@AfterReturning`：定义后置返回通知
- `@Around`：定义环绕通知
- `@AfterThrowing`：定义异常通知
- `@After`：定义后置最终通知

### 6.3.2. 实现
接口与实现类：
```java
public interface AnnotationInterface {
    void save();
    void modify();
    void delete();
}

@Component
public class AnnotationImpl implements AnnotationInterface {
    @Override
    public void save() {
        System.out.println("保存");
    }

    @Override
    public void modify() {
        System.out.println("修改");
    }

    @Override
    public void delete() {
        System.out.println("删除");
//        注释下面这行语句开启异常通知
//        int a = 1/0;
    }
}
```
切面类：
```java
@Aspect
@Component
public class AnnotationAspect {

    @Pointcut("execution(* com.aspectj_annotation.AnnotationInterface.*(..))")
    public void pointcut(){}

    @Before("pointcut()")
    public void before()
    {
        System.out.println("前置通知");
    }

    @AfterReturning(value = "pointcut()")
    public void afterReturning()
    {
        System.out.println("后置返回通知");
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        System.out.println("环绕通知开始");
        Object object = proceedingJoinPoint.proceed();
        System.out.println("环绕通知结束");
        return object;
    }

    @AfterThrowing(value = "pointcut()",throwing = "e")
    public void except(Throwable e)
    {
        System.out.println("异常通知");
    }

    @After("pointcut()")
    public void after()
    {
        System.out.println("后置最终通知");
    }

}
```
测试类：
```java
public class AnnotationTest {
    public static void test()
    {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        AnnotationInterface annotationInterface = (AnnotationInterface)context.getBean("annotationImpl");
        annotationInterface.delete();
        annotationInterface.modify();
        annotationInterface.save();
    }
}
```
配置文件：
```xml
<context:component-scan base-package="com.aspectj_annotation"/>
<aop:aspectj-autoproxy/>
```
输出：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200916005627584.png)

值得注意的是输出的顺序变动了，首先是环绕通知而不是前置通知。至于为什么这样笔者也不是很清楚，欢迎大神留言补充。


# 7 参考源码
`Java`版：

- [Github](https://github.com/2293736867/CSBookNotes/tree/master/Spring/Chapter4/java_demo)
- [码云](https://gitee.com/u6b7b5fc3/CSBookNotes/tree/master/Spring/Chapter4/java_demo)
- [CODE.CHINA](https://codechina.csdn.net/qq_27525611/CSBookNotes/-/tree/master/Spring/Chapter4/java_demo)

`Kotlin`版：

- [Github](https://github.com/2293736867/CSBookNotes/tree/master/Spring/Chapter4/kotlin_demo)
- [码云](https://gitee.com/u6b7b5fc3/CSBookNotes/tree/master/Spring/Chapter4/kotlin_demo)
- [CODE.CHINA](https://codechina.csdn.net/qq_27525611/CSBookNotes/-/tree/master/Spring/Chapter4/kotlin_demo)
