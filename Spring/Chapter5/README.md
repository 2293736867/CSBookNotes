# Table of Contents

* [1 概述](#1-概述)
* [2 `JDBC Template`](#2-jdbc-template)
  * [2.1 配置](#21-配置)
  * [2.2 常用方法](#22-常用方法)
  * [2.3 示例](#23-示例)
    * [2.3.1 依赖](#231-依赖)
    * [2.3.2 配置文件](#232-配置文件)
    * [2.3.3 实体类](#233-实体类)
    * [2.3.4 数据访问层](#234-数据访问层)
    * [2.3.5 测试](#235-测试)
* [3 事务管理](#3-事务管理)
  * [3.1 编程式事务管理](#31-编程式事务管理)
    * [3.1.1 底层`API`实现](#311-底层api实现)
      * [3.1.1.1 事务定义](#3111-事务定义)
      * [3.1.1.2 具体执行流程](#3112-具体执行流程)
    * [3.1.2 基于`TransactionTemplate`](#312-基于transactiontemplate)
  * [3.2 声明式事务管理](#32-声明式事务管理)
    * [3.2.1 基于`XML`](#321-基于xml)
      * [3.2.1.1 配置文件](#3211-配置文件)
      * [3.2.1.2 测试](#3212-测试)
    * [3.2.2 基于`@Transactional`](#322-基于transactional)
      * [3.2.2.1 配置文件](#3221-配置文件)
      * [3.2.2.2 测试](#3222-测试)
* [4 参考源码](#4-参考源码)


# 1 概述
`Spring`为开发者提供了`JDBCTemplate`，可以简化很多数据库操作相关的代码，本文主要介绍`JDBCTemplate`的使用以及事务管理功能。

# 2 `JDBC Template`
## 2.1 配置
配置的话主要配置以下几项：

- 数据源：`org.springframework.jdbc.datasource.DriverManager.DataSource`
- 数据库驱动：`com.cj.mysql.jdbc.Driver`，这里采用的是`MySQL 8`，注意`MySQL 5.7`以下的驱动名字不同，另外若是其他数据库请对应修改
- 数据库`URL`：`jdbc:mysql://localhost:3306/test`，`MySQL`默认的`3306`端口，数据库`test`
- 数据库用户名
- 数据库密码
- `JDBC`模板：`org.springframework.jdbc.core.jdbcTemplate`

参考配置如下：
```xml
<bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
    <property name="driverClassName" value="com.mysql.cj.jdbc.Driver"/>
    <property name="url" value="jdbc:mysql://localhost:3306/test"/>
    <property name="username" value="test"/> 
    <property name="password" value="test"/>
</bean>
<bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
    <property name="dataSource" ref="dataSource"/>
</bean>
<context:component-scan base-package="pers.dao"/>
```

## 2.2 常用方法
- `int update(String sql,Object args[])`：增/删/改操作，使用`args`设置其中的参数，返回更新的行数
- `List<T> query(String sql,RowMapper<T> rowMapper,Object []args)`：查询操作，`rowMapper`将结果集映射到用户自定义的类中

## 2.3 示例
### 2.3.1 依赖
首先导入依赖：
```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-jdbc</artifactId>
    <version>5.2.9.RELEASE</version>
</dependency>

<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.21</version>
</dependency>
```
`MySQL`的版本请根据个人需要更改，或使用其他数据库的驱动。

### 2.3.2 配置文件
完整配置文件如下：
```xml
<?xml version="1.0" encoding="utf-8" ?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd 
       http://www.springframework.org/schema/context 
       https://www.springframework.org/schema/context/spring-context.xsd">

    <bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
        <property name="driverClassName" value="com.mysql.cj.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/test"/>
        <property name="username" value="test"/>
        <property name="password" value="test"/>
    </bean>
    <bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
        <property name="dataSource" ref="dataSource"/>
    </bean>
    <context:component-scan base-package="pers.dao"/>
</beans>
```

### 2.3.3 实体类
```java
public class MyUser {
    private Integer id;
    private String uname;
    private String usex;
}
```

### 2.3.4 数据访问层
添加`@Repository`以及`@RequiredArgsConstructor`：
```java
@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TestDao {
    private final JdbcTemplate template;

    public int update(String sql,Object[] args)
    {
        return template.update(sql,args);
    }

    public List<MyUser> query(String sql, Object[] args)
    {
        RowMapper<MyUser> mapper = new BeanPropertyRowMapper<>(MyUser.class);
        return template.query(sql,mapper,args);
    }
}
```
因为直接使用`@Autowired`的话会提示不推荐：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200926223252663.png)

所以利用了`Lombok`的注解`@RequiredArgsConstructor`，效果相当如下构造方法，只不过是简化了一点：
```java
@Autowired
public TestDao(JdbcTemplate template)
{
    this.template = template;
}
```
### 2.3.5 测试
测试之前先建表：
```sql
create table MyUser(
    id INT AUTO_INCREMENT PRIMARY KEY ,
    uname varchar(20),
    usex varchar(20)
)
```
测试类：
```java
public class Main {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        TestDao dao = (TestDao)context.getBean("testDao");
        String insertSql = "insert into MyUser(uname,usex) values(?,?)";
        String[] param1 = {"chenhengfa1","男"};
        String[] param2 = {"chenhengfa2","男"};
        String[] param3 = {"chenhengfa3","男"};
        String[] param4 = {"chenhengfa4","男"};

        dao.update(insertSql,param1);
        dao.update(insertSql,param2);
        dao.update(insertSql,param3);
        dao.update(insertSql,param4);

        String selectSql = "select * from MyUser";
        List<MyUser> list = dao.query(selectSql,null);
        for(MyUser mu:list)
        {
            System.out.println(mu);
        }
    }
}
```
输出：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200926225137360.png)

如果出现异常或插入不成功等其他情况，请检查`SQL`语句是否编写正确，包括表名以及字段名。

# 3 事务管理
`Spring`中的事务管理有两种方法：

- 编程式事务管理：代码中显式调用`beginTransaction`、`commit`、`rollback`等就是编程式事务管理
- 声明式事务管理：通过`AOP`实现，不需要通过编程方式管理事务，因此不需要再业务逻辑代码中掺杂事务处理的代码，开发更加简单，便于后期维护

下面先来看一下编程式事务管理的实现。

## 3.1 编程式事务管理
编程式事务管理的配置又有两种方法：

- 基于底层`API`
- 基于`TransactionTemplate`

需要的依赖如下：

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-tx</artifactId>
    <version>5.2.9.RELEASE</version>
</dependency>

<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-expression</artifactId>
    <version>5.2.9.RELEASE</version>
</dependency>

<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-aop</artifactId>
    <version>5.2.9.RELEASE</version>
</dependency>

<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.9.6</version>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-aspects</artifactId>
    <version>5.2.9.RELEASE</version>
</dependency>
```
### 3.1.1 底层`API`实现
根据`PlatformTransactionManager`、`TransactionDefinition`、`TransactionStatus`几个核心接口，通过编程方式进行事务管理，首先配置事务管理器：

```xml
<bean id="txManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
    <property name="dataSource" ref="dataSource"/>
</bean>
```

接着修改数据库访问类：

```java
@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TestDao {
    private final JdbcTemplate template;
    private final DataSourceTransactionManager manager;

    public int update(String sql,Object[] args)
    {
        return template.update(sql,args);
    }

    public List<MyUser> query(String sql,Object[] args)
    {
        RowMapper<MyUser> mapper = new BeanPropertyRowMapper<>(MyUser.class);
        return template.query(sql,mapper,args);
    }

    public void testTransaction()
    {
        TransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus status = manager.getTransaction(definition);
        String message = "执行成功，没有事务回滚";

        try
        {
            String sql1 = "delete from MyUser";
            String sql2 = "insert into MyUser(id,uname,usex) values(?,?,?)";
            Object [] param2 = {1,"张三","男"};
            template.update(sql1);
            template.update(sql2,param2);
            template.update(sql2,param2);
            manager.commit(status);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            manager.rollback(status);
            message = "主键重复，事务回滚";
        }
        System.out.println(message);
    }
}
```
#### 3.1.1.1 事务定义
`TransactionDefinition`是事务定义，是一个接口：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200927075442740.png)

主要定义了：

- 事务隔离级别
- 事务传播行为
- 事务超时时间
- 是否为只读事务

而`DefaultTransactionDefinition`就是上面属性的一些默认配置，比如：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200927075731236.png)

也就是定义了：

- 传播行为为`0`：也就是常量`PROPAGATION_REQUIREDE`，表示如果当前存在一个事务，则加入当前事务，如果不存在任何事务，就创建一个新事务
- 隔离级别为`-1`：这个也是`TransactionDefinition`的默认参数，表示使用数据库的默认隔离级别，通常情况下为`Read Committed`
- 超时为`-1`：默认设置不超时，如需要设置超时请调用`setTimeout`方法，比如如果设置为了`60`，那么相当于如果操作时间超过了`60s`，而且后面还涉及到`CRUD`操作，那么会抛出超时异常并回滚，如果超时操作的后面没有涉及到`CRUD`操作，那么不会回滚
- 只读事务为`false`：默认为`false`，但是该变量不是表明“不能”进行修改等操作，而是一种暗示，如果不包含修改操作，那么`JDBC`驱动和数据库就有可能针对该事务进行一些特定的优化

#### 3.1.1.2 具体执行流程
具体执行流程如下：

- 定义事务：实例类为`DefaultTransactionDefinition`
- 开启事务：通过`getTransaction(TransactionDefinition)`开启
- 执行业务方法
- 根据业务方法是否出现异常手动调用`DataSourceTransaction`的`commit(TransactionStatus)`进行提交
- 出现异常调用`rollback(TransactionStatus)`进行回滚

测试如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200926230605773.png)

### 3.1.2 基于`TransactionTemplate`
步骤：

- 通过调用`TransactionTemplate`的`execute`实现
- `execute`接受一个`TransactionCallback`接口参数
- `TransactionCallback`定义了一个`doInTransaction`方法
- 通常以匿名内部类的方式实现`TransactionCallback`接口，在其中的`doInTransaction`编写业务逻辑代码
- `doInTransaction`有一个`TransactionStatus`的参数，可以调用`setRollbackOnly`进行回滚

默认的回滚规则如下：

- 如果抛出未检查异常或者手动调用`setRollbackOnly`，则回滚
- 如果执行完成或抛出检查异常，则提交事务

示例如下，首先编写配置文件对`Bean`进行注入：

```xml
<!--事务管理器-->
<bean id="txManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="dataSource"/>
</bean>
<!--事务模板-->
<bean id="transactionTemplate" class="org.springframework.transaction.support.TransactionTemplate">
    <property name="transactionManager" ref="txManager"/>
</bean>
```
其次修改数据访问类，添加一个测试方法：
```java
public void testTransactionTemplate()
{
    System.out.println(transactionTemplate.execute((TransactionCallback<Object>) transactionStatus -> {
        String deleteSql = "delete from MyUser";
        String insertSql = "insert into MyUser(id,uname,usex) values(?,?,?)";
        Object[] parm = {1, "张三", "男"};
        try {
            template.update(deleteSql);
            template.update(insertSql, parm);
            template.update(insertSql, parm);
        } catch (Exception e) {
            message = "主键重复，事务回滚";
            e.printStackTrace();
        }
        return message;
    }));
}
```
大部分代码与第一个例子类似就不解释了，结果也是因为主键重复出现异常，造成事务回滚：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200926231620192.png)

## 3.2 声明式事务管理
`Spring`声明式事务管理通过`AOP`实现，本质是在方法前后进行拦截，在目标方法开始之前创建或加入一个事务，执行目标方法完成之后根据执行情况提交或回滚事务。相比起编程式事务管理，声明式最大的优点就是不需要通过编程的方式管理事务，业务逻辑代码无需混杂事务代码，但是唯一不足的地方就是最细粒度只能作用到方法上，而不能做到代码块级别。

实现方式有如下两种：

- 基于`XML`实现
- 基于`@Transactional`实现

### 3.2.1 基于`XML`
`Spring`提供了`tx`命令空间来配置事务：

- `<tx:advice>`：配置事务通知，一般需要指定`id`以及`transaction-manager`
- `<tx:attributes>`：配置多个`<tx:method>`指定执行事务的细节

#### 3.2.1.1 配置文件
完整配置文件如下：
```xml
<?xml version="1.0" encoding="utf-8" ?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:cache="http://www.springframework.org/schema/cache"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context
       https://www.springframework.org/schema/context/spring-context.xsd
       http://www.springframework.org/schema/cache
       http://www.springframework.org/schema/cache/spring-cache.xsd
       http://www.springframework.org/schema/tx
       http://www.springframework.org/schema/tx/spring-tx.xsd
       http://www.springframework.org/schema/aop
       http://www.springframework.org/schema/aop/spring-aop.xsd"
>

    <bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
        <property name="driverClassName" value="com.mysql.cj.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/test"/>
        <property name="username" value="test"/>
        <property name="password" value="test"/>
    </bean>
    <bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
        <property name="dataSource" ref="dataSource"/>
    </bean>
    <context:component-scan base-package="pers.dao"/>
    <!--事务管理器-->
    <bean id="txManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="dataSource"/>
    </bean>

    <bean id="transactionTemplate" class="org.springframework.transaction.support.TransactionTemplate">
        <property name="transactionManager" ref="txManager"/>
    </bean>

	<!--声明式事务-->
    <tx:advice id="myAdvice" transaction-manager="txManager">
        <tx:attributes>
        	<!--任意方法-->
            <tx:method name="*" />
        </tx:attributes>
    </tx:advice>
    <!--aop配置，具体可以看笔者之前的文章-->
    <aop:config>
    	<!--定义切点，执行testXMLTranscation()时进行增强-->
        <aop:pointcut id="txPointCut" expression="execution(* pers.dao.TestDao.testXMLTransaction())"/>
        <!--切面-->
        <aop:advisor advice-ref="myAdvice" pointcut-ref="txPointCut"/>
    </aop:config>
</beans>
```

#### 3.2.1.2 测试
测试方法如下：
```java
public void testXMLTransaction()
{
    String deleteSql = "delete from MyUser";
    String saveSql = "insert into MyUser(id,uname,usex) values(?,?,?)";
    Object [] parm = {1,"张三","男"};
    template.update(deleteSql);
    template.update(saveSql,parm);
    template.update(saveSql,parm);
}
```
运行结果：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200927071730346.png)

可以看到提示主键重复了。

### 3.2.2 基于`@Transactional`
`@Transactional`一般作用于类上，使得该类所有`public`方法都具有该类型的事务属性。下面创建一个示例。

#### 3.2.2.1 配置文件
将上一个例子中的`<aop:config>`以及`<tx:advice>`注释掉，同时添加：
```xml
<!--事务管理的注解驱动器-->
<tx:annotation-driven transaction-manager="txManager"/>
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200927071953724.png)

#### 3.2.2.2 测试
测试方法与上一个例子一致，结果也是如此：

![在这里插入图片描述](https://img-blog.csdnimg.cn/2020092707204691.png)


# 4 参考源码
每一个示例都给出了独立包下的源码，`Java`版：

- [Github]()
- [码云]()
- [CODE.CHINA]()

`Kotlin`版：

- [Github]()
- [码云]()
- [CODE.CHINA]()




