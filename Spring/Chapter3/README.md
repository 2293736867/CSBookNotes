# 1 `Bean`配置
`Spring`可以看做是一个管理`Bean`的工厂，开发者需要将`Bean`配置在`XML`或者`Properties`配置文件中。实际开发中常使用`XML`的格式，其中`<bean>`中的属性或子元素如下：

- `id`：`Bean`在`BeanFactory`中的唯一标识，在代码中通过`BeanFactory`获取`Bean`的实例时候需要以此作为索引
- `class`：`Bean`的具体实体类，使用`包名+类名`的形式指定
- `scope`：指定`Bean`实例的作用域
- `<constructor-arg>`：使用构造方法注入，指定构造方法的参数，`index`表示序号，`ref`指定对`BeanFactory`中其他`Bean`的引用关系，`type`指定参数类型，`value`指定参数常量值
- `<property>`：用于设置一个属性，表示使用`setter`注入，`name`指定属性的名字，`value`指定要注入的值，`ref`指定注入的某个`Bean`的`id`
- `<list>`：用于封装`List`或者数组类型的依赖注入 
- `<map>`：封装`Map`类型的依赖注入
- `<set>`：封装`Set`类型的依赖注入
- `<entry>`：`<map>`的子元素，用于设置一个键值对

# 2 `Bean`实例化
`Spring`实例化`Bean`有三种方式：

- 构造方法实例化
- 静态工厂实例化
- 实例工厂实例化

下面进行简单的演示。

## 2.1 构造方法实例化
`Spring`可以调用`Bean`对应的类的无参构造方法进行实例化，比如：
```java
public class TestBean {
    public TestBean()
    {
        System.out.println("构造方法实例化");
    }
}
```
配置文件如下：
```xml
<bean id="testBean" class="TestBean"/>
```
则会调用无参构造方法初始化。

其实就是只写一个`<bean>`就可以了，默认的话会调用无参构造方法初始化。


## 2.2 静态工厂实例化
静态工厂实例化需要在工厂类中配置一个静态方法来创建`Bean`，并添加`factory-method`元素，首先创建工厂类：
```java
public class TestBeanFactory {
    private static final TestBean testBean = new TestBean();
    public static TestBean getInstance()
    {
        return testBean;
    }
}
```
接着配置文件通过`class`指定该工厂类，通过`factory-method`指定获取实例的方法：
```xml
<bean id="testBeanFactory" class="TestBeanFactory" factory-method="getInstance"/>
```
这样就可以通过`id`获取了：
```java
TestBean test = (TestBean) context.getBean("testBeanFactory");
```


## 2.3 实例工厂实例化
实例工厂实例化与静态工厂实例化类似，不过是非静态方法，然后加上一个`factory-bean`元素，同样首先创建工厂类：
```java
public class TestBeanFactory {
    public TestBean getInstance()
    {
        return new TestBean();
    }
}
```
在配置文件需要添加两个`Bean`，一个指定工厂类，一个指定使用哪一个工厂类以及使用工厂类的哪一个方法：
```xml
<bean id="factory" class="TestBeanFactory" /> <!--指定工厂类-->
<bean id="testBeanFactory" factory-bean="factory" factory-method="getInstance" /> <!--指定工厂Bean以及哪一个工厂方法-->
```
获取：
```java
TestBean test = (TestBean) context.getBean("testBeanFactory");
```

# 3 `Bean`作用域
## 3.1 分类
`<bean>`中的`scope`可以指定的作用域如下：

- `singleton`：默认作用域，在`Spring`容器只有一个`Bean`实例
- `prototype`：每次获取`Bean`都会返回一个新的实例
- `request`：在一次`HTTP`请求中只返回一个`Bean`实例，不同`HTTP`请求返回不同的`Bean`实例，仅在`Spring Web`应用程序上下文使用
- `session`：在一个`HTTP Session`中，容器将返回同一个`Bean`实例，仅在`Spring Web`应用程序上下文中使用
- `application`：为每个`ServletContext`对象创建一个实例，即同一个应用共享一个`Bean`实例，仅在`Spring Web`应用程序上下文使用
- `websocket`：为每个`WebSocket`对象创建一个`Bean`实例，仅在`Spring Web`应用程序上下文使用

下面具体说一下最常用的两个：`singleton`和`prototype`。

## 3.2 `singleton`
`scope`设置为`singleton`时，`Spring IoC`仅生成和管理一个`Bean`实例，使用`id`/`name`获取`Bean`实例时，`IoC`容器返回共享的`Bean`实例。设置方式如下：
```xml
<bean id="testBean" class="TestBean"/>
<bean id="testBean" class="TestBean" scope="singleton"/>
```
因为这是默认的作用域，设置的话`IDE`也智能提示是多余的：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200909075954376.png)

所以通过不需要加上`scope`，测试例子：
```java
TestBean test1 = (TestBean) context.getBean("testBean");
TestBean test2 = (TestBean) context.getBean("testBean");
System.out.println(test1 == test2);
```
输入的结果为`True`。

## 3.3 `prototype`
每次获取`Bean`时都会创建一个新的实例，例子如下：
```xml
<bean id="testBean" class="TestBean" scope="prototype"/>
```
```java
TestBean test1 = (TestBean) context.getBean("testBean");
TestBean test2 = (TestBean) context.getBean("testBean");
System.out.println(test1 == test2);
```
测试结果为`False`。

# 4 `Bean`生命周期
`Spring`可以管理作用域为`singleton`的生命周期，在此作用域下`Spring`能精确知道`Bean`何时被创建，何时初始化完成以及何时被摧毁。`Bean`的整个生命周期如下：

- 实例化`Bean`
- 进行依赖注入
- 如果`Bean`实现了`BeanNameAware`，调用`setBeanName`
- 如果`Bean`实现了`BeanFactoryAware`，调用`setBeanFactory`
- 如果`Bean`实现了`ApplicationContextAware`，调用`setApplicationContext`
- 如果`Bean`实现了`BeanPostProcessor`，调用`postProcessBeforeInitialization`
- 如果`Bean`实现了`InitializingBean`，调用`afterPropertiesSet`
- 如果配置文件配置了`init-method`属性，调用该方法
- 如果实现了`BeanPostProcessor`，调用`postProcessAfterInitialization`，注意接口与上面的相同但是方法不一样
- 不需要时进入销毁阶段
- 如果`Bean`实现了`DisposableBean`，调用`destroy`
- 如果配置文件配置了`destroy-method`，调用该方法

下面用代码进行演示：

```java
public class TestBean implements BeanNameAware, BeanFactoryAware, ApplicationContextAware, BeanPostProcessor, InitializingBean, DisposableBean {
    public TestBean()
    {
        System.out.println("调用构造方法");
    }

    @Override
    public void setBeanName(String s) {
        System.out.println("调用BeanNameAware的setBeanName");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("调用BeanFactoryAware的setBeanFactory");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("调用ApplicationContextAware的setApplicationContext");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("调用BeanPostProcessor的postProcessBeforeInitialization");
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("调用InitializingBean的afterPropertiesSet");
    }

    public void initMethod()
    {
        System.out.println("调用XML配置的init-method");
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("调用BeanPostProcessor的postProcessAfterInitialization");
        return null;
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("调用DisposableBean的destroy");
    }

    public void destroyMethod()
    {
        System.out.println("调用XML配置的destroy-method");
    }
}
```
配置文件如下，指定了`init-method`以及`destroy-method`：
```xml
<bean id="testBean" class="TestBean" init-method="initMethod" destroy-method="destroyMethod"/>
```
测试：
```java
public static void main(String[] args) {
    ConfigurableApplicationContext context = new FileSystemXmlApplicationContext("classpath:applicationContext.xml");
    TestBean test = (TestBean) context.getBean("testBean");
    ((BeanDefinitionRegistry) context.getBeanFactory()).removeBeanDefinition("testBean");
}
```
输出如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200909125748530.png)

如果没有最后一行的手动删除`Bean`定义是不会看见最后两行的输出的，另外，这里没有调用`BeanPostProcessor`接口的两个方法，如果把`scope`改为`prototype`，输出如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/2020090912593951.png)

可以看到首先对`Bean`进行一次初始化，并且再次生成一个新的实例，而且调用了`BeanPostProcessor`的两个方法。但是需要注意`Spring`不会管理`scope`为`prototype`的销毁，所以图中没有看到调用销毁的方法。


# 5 `Bean`装配方式
`Spring`支持以下两种装配方式：

- 基于`XML`装配
- 基于注解装配
- 显式`Bean`装配

`Bean`的装配方式也就是`Bean`的依赖注入方式，下面分别进行阐述。


## 5.1 基于`XML`装配
基于`XML`装配也就是在`XML`文件中指定使用构造方法注入或者`setter`注入，比如：
```java
public class TestBean {
    private final List<String> stringList;
    private String s;

    public TestBean(List<String> stringList) {
        this.stringList = stringList;
    }

    public void setS(String s)
    {
        this.s = s;
    }

    @Override
    public String toString() {
        return stringList.toString() + "\n" + s + "\n";
    }
}
```
该`Bean`有一个带参数的构造方法以及一个`setter`，接着在`XML`中指定相应的值即可：
```xml
<bean id="testBean" class="TestBean">
    <constructor-arg index="0">
        <list>
            <value>1</value>
            <value>2</value>
        </list>
    </constructor-arg>
    <property name="s" value="444" />
</bean>
```
测试：
```java
ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
System.out.println(context.getBean("testBean"));
```

## 5.2 基于注解装配
尽管`XML`方式可以简单地装配`Bean`，但是一旦`Bean`过多就会造成`XML`文件过于庞大，不方便以后的升级和维护，因此推荐使用基于注解的装配方式，先来看一下常用的注解：

- `@Autowired`：自动装配，默认按照`Bean`的类型进行装配，这是`Spring`的注解
- `@Resource`：与`@Autowired`类似，但是是按名称进行装配，当找不到与名称匹配的`Bean`时才按照类型进行装配，这是`JDK`的注解
- `@Qualifier`：与`@Autowired`配合使用，因为`@Autowired`默认按`Bean`类型进行装配，使用`@Qualifier`可以按名称进行装配
- `@Bean`：方法上的注解，用于产生一个`Bean`，然后交由`Spring`管理
- `@Component`：表示一个组件对象，加上了该注解就能实现自动装配，默认的`Bean`的`id`为使用小驼峰命名法的类
- `@Repository`/`@Service`/`@Controller`：实际上是`@Component`的别名，只不过是专门用于持久层/业务层/控制层的，从源码可以看出三个注解的定义除了名字不一样其他都一致，并且都是`@Component`的别名：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200908201601356.PNG)

官方文档也提到相比起使用`@Component`，使用`@Repository`/`@Service`/`@Controller`在持久层/业务层/控制层更加合适，而不是统一使用`@Component`：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200908201800199.PNG)

## 5.3 注解使用示例

### 5.3.1 `@Bean`

`@Bean`示例如下：
```java
public class TestBean implements BeanNameAware{
    @Override
    public void setBeanName(String s) {
        System.out.println("setBeanName");
    }
}

@Configuration
public class Config {
    @Bean
    public TestBean getBean()
    {
        return new TestBean();
    }
}

public class Main {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
        context.getBean("getBean");
    }
}
```
注意通过`@Bean`自动产生的`Bean`的`id`为方法名，而不是`Bean`的类名的小驼峰形式。

### 5.3.2 其他
`@Autowired`/`@Resource`/`@Qualifier`/`@Repository`/`@Service`/`@Controller`综合示例，首先创建如下包以及文件：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200909155801661.png)
```java
@Controller
public class TestController {
    @Resource
    private TestService service;

    public void save()
    {
        System.out.println("controller save");
        service.save();
    }
}
```

```java
@Service
public class TestService {
    @Autowired
    @Qualifier("testRepository1")
    private TestInterface repository1;

    @Autowired
    @Qualifier("testRepository2")
    private TestInterface repository2;
    public void save()
    {
        System.out.println("service save");
        repository1.save();
        repository2.save();
    }
}
```

```java
@Repository
public class TestRepository1 implements TestInterface{
    @Override
    public void save() {
        System.out.println("repository1 save");
    }
}
```

```java
@Repository
public class TestRepository2 implements TestInterface{
    @Override
    public void save() {
        System.out.println("repository2 save");
    }
}
```


```java
public interface TestInterface {
    void save();
}
```

```java
public class Main {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        ((TestController)context.getBean("testController")).save();
    }
}
```
配置文件：
```xml
<context:component-scan base-package="bean" />
```

在`TestService`中，使用了`@Qualifier`：
```java
@Autowired
@Qualifier("testRepository1")
private TestInterface repository1;

@Autowired
@Qualifier("testRepository2")
private TestInterface repository2;
```
因为`TestInterface`有两个实现类，`@Autowired`不知道是选择`TestRepository1`还是`TestRepository2`，因此需要加上`@Qualifier`，指定需要注入的`Bean`的`id`，或者使用`@Resouce`：
```java
@Resource
private TestInterface testRepository1;

@Resource
private TestInterface testRepository2;
```
但是要注意这样默认了成员的名字就是`Bean`的`id`，可以看到这里的名字是`testRepository1`与`testRepository2`而不是`repository1`和`repository2`。
