# 1 概述
`MyBaits`是一个著名的持久层框架，本文首先介绍了`MyBatis`的简单使用，接着与`Spring`进行整合，最后简单地使用了`Generator`去自动生成代码。

# 2 `MyBatis`简介
`MyBatis`本来是`Apache`的一个开源项目——`iBatis`，2010年由`Apaceh Software Foundation`迁移到了`Google Code`，并改名为`MyBatis`。

`MyBatis`是一个基于`Java`的持久层框架，提供的持久层框架包括`SQL Maps`和`Data Access Objects`，使用简单的`XML`或者注解用于配置映射，将接口和`POJO`映射成数据库中的记录，是一个小巧、方便、高效、简单、直接、半自动化的持久层框架。

# 3 工作原理
上图：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200929080425358.png)


- 读取配置文件：`mybatis-config.xml`是全局`MyBatis`配置文件，配置了`MyBatis`运行环境信息
- 加载映射文件：也就是`SQL`映射文件，配置了操作数据库的`SQL`语句
- 构造会话工厂：通过配置文件构造会话工厂`SqlSessionFactory`
- 创建会话对象：由上一步的会话工厂创建会话对象`SqlSession`
- 获取`MapperStatement`：通过用户调用的`api`的`Statement ID`获取`MapperStatement`对象
- 输入参数映射：通过`Executor`对`MapperStatement`进行解析，将各种`Java`基本类型转化为`SQL`操作语句中的类型
- 输出结果映射：`JDBC`执行`SQL`后，借助`MapperStatement`的映射关系将返回结果转化为`Java`基本类型并返回

# 4 `MyBatis`示例
首先先来看一下纯`MyBaits`的示例，没有整合`Spring`，一个简单的`Maven`工程，项目结构如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200928184105917.png)

## 4.1 依赖
```xml
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis</artifactId>
    <version>3.5.5</version>
</dependency>

<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.12</version>
</dependency>

<!--驱动用的是MySQL，版本请自行修改-->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.21</version>
</dependency>
```
`Gradle`：
```bash
compile group: 'org.mybatis', name: 'mybatis', version: '3.5.5'
compile group: 'mysql', name: 'mysql-connector-java', version: '8.0.21'
```

## 4.2 实体类
```java
@Setter
@Getter
@Builder
public class User {
    private Integer id;
    private String name;

    @Override
    public String toString() {
        return "id:"+id+"\tname:"+name;
    }
}
```
## 4.3 映射文件
新建一个叫`UserMapper.xml`的映射文件：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="UserMapper">
    <select id="selectById" parameterType="Integer" resultType="pers.entity.User">
        select * from user where id=#{id}
    </select>
    <select id="selectAll" resultType="pers.entity.User">
        select * from user
    </select>

    <insert id="insert" parameterType="pers.entity.User">
        INSERT INTO `user` (`id`,`name`)
        VALUES (#{id},#{name})
    </insert>

    <update id="update" parameterType="pers.entity.User">
        UPDATE `user` set `name`=#{name} where id=#{id}
    </update>

    <delete id="delete" parameterType="Integer">
        DELETE FROM `user` WHERE `id` = #{id}
    </delete>
</mapper>
```
映射文件是一个`XML`文件，根元素为`<mapper>`，需要注意其中的`namespace`属性，调用的时候通过该`namespace`调用。其中的子元素表示`SQL`语句：

- `<select>`：查询，`id`指定了这条语句的`id`号，调用时通过`namespace.id`的方式调用，比如该条`select`需要通过`UserMapper.selectById`调用，`parameterType`指定参数类型，这里是一个`Integer`的参数，`resultType`指定返回类型，实体类
- `<insert>`/`<update>`/`<delete>`：对应的插入/修改/删除语句
- 关于占位符：`#{}`表示是占位符，相当于传统`JDBC`中的`?`，`#{id}`表示该占位符等待接收的参数名称为`id`


## 4.4 配置文件
`MyBatis`的配置文件，叫`mybatis-config.xml`：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN" "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC" />
            <dataSource type="POOLED">
                <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
                <property name="url" value="jdbc:mysql://localhost:3306/test"/>
                <property name="username" value="test"/>
                <property name="password" value="test"/>
            </dataSource>
        </environment>
    </environments>

    <mappers>
        <mapper resource="mapper/UserMapper.xml" />
    </mappers>
</configuration>
```
指定了数据库的一些连接属性还有`mapper`的位置。

## 4.5 测试
```java
public class Main {
    public static void main(String[] args) {
        try
        {
            InputStream inputStream = Resources.getResourceAsStream("config/mybatis-config.xml");
            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(inputStream);
            SqlSession session = factory.openSession();
            User user = session.selectOne("UserMapper.selectById",1);
            System.out.println(user);
            User user1 = User.builder().name("test").build();
            session.insert("UserMapper.insert",user1);
            user1.setName("222");
            session.update("UserMapper.update",user1);
            List<User> list = session.selectList("UserMapper.selectAll");
            list.forEach(System.out::println);
            session.delete("UserMapper.delete",1);
            session.commit();
            session.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
```
主要流程如下：

- 读取配置文件：根据`org.apache.ibatis.io.Resources`读取配置文件`mybatis-config.xml`，请注意配置文件的位置正确，这里的配置文件都放在`resources`下，`mybatis-config.xml`放在其中的`config`下
- 构建`Session`：根据配置文件构建`SqlSessionFactory`后，通过`openSession`创建`Session`
- 业务操作：通过`session`的`selectOne/insert/update`等进行业务操作，这类操作带两个参数，第一个参数是`String`，表示配置文件中的`SQL`语句，采用`namespace.id`的形式，比如这里的`UserMapper.xml`中声明`namespace`为`UserMapper`，其中带有一条`id`为`selectById`的`select`语句，因此调用时使用`UserMapper.selectById`的形式，第二个参数是一个`Object`，表示要传递的参数，也就是绑定到配置文件中对应占位符的值
- 提交与关闭：业务操作完成后提交事务并关闭`session`

示例测试结果：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200928183501487.png)

# 5 `Spring`整合示例
上面的例子只是为了演示`MyBatis`的基本使用，没有整合`Spring`，这里的例子是把`Spring`整合进来，流程也大概差不多，项目结构如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200929070913641.png)

## 5.1 依赖
分为5类`JAR`：

- `MyBatis`需要的`JAR`
- `Spring`需要的`JAR`
- `MyBatis`与`Spring`整合的中间`JAR`
- 数据库驱动`JAR`
- 数据源`JAR`

完整依赖如下：
```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>5.2.9.RELEASE</version>
</dependency>

<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-core</artifactId>
    <version>5.2.9.RELEASE</version>
</dependency>

<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-beans</artifactId>
    <version>5.2.9.RELEASE</version>
</dependency>

<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
    <version>5.2.9.RELEASE</version>
</dependency>

<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.12</version>
</dependency>

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

<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-tx</artifactId>
    <version>5.2.9.RELEASE</version>
</dependency>

<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis</artifactId>
    <version>3.5.5</version>
</dependency>

<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis-spring</artifactId>
    <version>2.0.5</version>
</dependency>

<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-dbcp2</artifactId>
    <version>2.8.0</version>
</dependency>
```

`Gradle`：
```bash
compile group: 'org.springframework', name: 'spring-beans', version: '5.2.9.RELEASE'
compile group: 'org.springframework', name: 'spring-context', version: '5.2.9.RELEASE'
compile group: 'org.springframework', name: 'spring-core', version: '5.2.9.RELEASE'
compile group: 'org.springframework', name: 'spring-tx', version: '5.2.9.RELEASE'
compile group: 'org.springframework', name: 'spring-jdbc', version: '5.2.9.RELEASE'
compile group: 'mysql', name: 'mysql-connector-java', version: '8.0.21'
compile group: 'org.apache.commons', name: 'commons-dbcp2', version: '2.8.0'
compile group: 'org.mybatis', name: 'mybatis', version: '3.5.5'
compile group: 'org.mybatis', name: 'mybatis-spring', version: '2.0.5'
```


## 5.2 配置文件
配置文件分为三类：

- `MyBatis`映射文件：编写`mapper`的地方，也就是业务需要的`SQL`语句
- `MyBatis`全局配置文件：由于整合了`Spring`，数据源的配置放在了`Spring`的配置文件中，而只需要保留`mapper`的查找位置
- `Spring`配置文件：配置数据源+事务管理+`MyBaits`的`sqlSssionFactory`+组件扫描

### 5.2.1 `MyBatis`映射文件
与上面的例子差不多，只是修改了`namespace`为`包名.类名`的形式：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="pers.dao.UserDao">
    <select id="selectById" parameterType="Integer" resultType="pers.entity.User">
        select * from user where id=#{id}
    </select>
    <select id="selectAll" resultType="pers.entity.User">
        select * from user
    </select>

    <insert id="insert" parameterType="pers.entity.User">
        INSERT INTO `user` (`id`,`name`)
        VALUES (#{id},#{name})
    </insert>

    <update id="update" parameterType="pers.entity.User">
        UPDATE `user` set `name`=#{name} where id=#{id}
    </update>

    <delete id="delete" parameterType="Integer">
        DELETE FROM `user` WHERE `id` = #{id}
    </delete>
</mapper>
```
`namespace`需要与对应包名的带有`@Mapper`的类配置一致。

### 5.2.2 `MyBatis`配置文件
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN" "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <mappers>
        <mapper resource="mapper/UserMapper.xml" />
    </mappers>
</configuration>
```
### 5.2.3 `Spring`配置文件
```xml
<?xml version="1.0" encoding="utf-8" ?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/tx
       http://www.springframework.org/schema/tx/spring-tx.xsd
       http://www.springframework.org/schema/context
       https://www.springframework.org/schema/context/spring-context.xsd"
       >

    <context:component-scan base-package="pers.dao"/>
    <context:component-scan base-package="pers.service"/>
    <!--数据源-->
    <bean id="dataSource" class="org.apache.commons.dbcp2.BasicDataSource">
        <property name="driverClassName" value="com.mysql.cj.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/test"/>
        <property name="username" value="test"/>
        <property name="password" value="test"/>
        <!--最大连接数+最大空闲数+初始连接数-->
        <property name="maxTotal" value="30"/>
        <property name="maxIdle" value="10"/>
        <property name="initialSize" value="5"/>
    </bean>
    
    <!--事务管理-->
    <bean id="txManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource"  ref="dataSource"/>
    </bean>

	<!--开启事务注解-->
    <tx:annotation-driven transaction-manager="txManager" />

	<!--创建SqlSessionFactory Bean-->
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource"/>
        <property name="configLocation" value="classpath:config/mybatis-config.xml"/>
    </bean>

	<!--注解扫描，主要用于@Mapper，会扫描basePackge下的@Mapper注解的接口并自动装配为MyBatis的映射接口-->
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="basePackage" value="pers.dao"/>
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
    </bean>
</beans>
```
## 5.3 持久层
需要加上`@Mapper`注解，表示自动装配为`MyBatis`的映射接口，注意：

- 映射文件中的`namespace`需要与`包名.类名`对应，比如这里的包为`pers.dao`，类名为`UserDao`，那么映射文件中的`namespace`为`pers.dao.UserDao`
- `id`需要与方法名对应，比如映射文件中的有一条`select`语句的`id`为`selectById`，那么方法就需要命名为`selectById`，且参数类型需要对应一致
```java
@Repository
@Mapper
public interface UserDao {
    User selectById(Integer id);
    List<User> selectAll();
    int insert(User user);
    int update(User user);
    int delete(Integer id);
}
```
## 5.4 业务层
```java
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MyBatisService {
    private final UserDao dao;

    public void test(){
        User user = dao.selectById(13);
        System.out.println(user);
        dao.insert(User.builder().name("333").build());
        dao.update(User.builder().name("88888").id(13).build());
        dao.selectAll().forEach(System.out::println);
        dao.delete(12);
        dao.selectAll().forEach(System.out::println);
    }
}
```
注入`UserDao`后进行简单的测试，结果如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200929070332272.png)

# 6 自动生成代码
相信很多程序员也讨厌写又长又麻烦的`XML`配置文件，因此，`MyBatis`也提供了一个生成器插件，可以直接从表中生成实体类、`dao`接口以及映射文件，可以省去很多操作。

步骤如下：

- 导入依赖
- 编写`Generator`配置文件
- 生成代码

## 6.1 依赖
其实就是加入一个插件：
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.mybatis.generator</groupId>
            <artifactId>mybatis-generator-maven-plugin</artifactId>
            <version>1.4.0</version>
            <configuration>
                <!-- 在控制台打印执行日志 -->
                <verbose>true</verbose>
                <!-- 重复生成时会覆盖之前的文件-->
                <overwrite>true</overwrite>
                <configurationFile>src/main/resources/generatorConfig.xml</configurationFile>
            </configuration>
            <dependencies>
                <dependency>
                    <groupId>mysql</groupId>
                    <artifactId>mysql-connector-java</artifactId>
                    <version>8.0.21</version>
                </dependency>
            </dependencies>
        </plugin>
    </plugins>
</build>
```
数据库驱动请对应修改。

至于`Gradle`版请看`Kotlin`版源码。

## 6.2 配置文件
这里是参考别人的配置文件，修改数据库连接、表名、包名即可：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration
        PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
        "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">
<generatorConfiguration>
    <!-- context 是逆向工程的主要配置信息 -->
    <!-- id：起个名字 -->
    <!-- targetRuntime：设置生成的文件适用于那个 mybatis 版本 -->
    <context id="default" targetRuntime="MyBatis3">
        <!--optional,指在创建class时，对注释进行控制-->
        <commentGenerator>
            <property name="suppressDate" value="true"/>
            <!-- 是否去除自动生成的注释 true：是 ： false:否 -->
            <property name="suppressAllComments" value="true"/>
        </commentGenerator>
        <!--jdbc的数据库连接 wg_insert 为数据库名字-->
        <jdbcConnection driverClass="com.mysql.cj.jdbc.Driver" connectionURL="jdbc:mysql://localhost:3306/test" userId="test" password="test" />
        <!--非必须，类型处理器，在数据库类型和java类型之间的转换控制-->
        <javaTypeResolver>
            <!-- 默认情况下数据库中的 decimal，bigInt 在 Java 对应是 sql 下的 BigDecimal 类 -->
            <!-- 不是 double 和 long 类型 -->
            <!-- 使用常用的基本类型代替 sql 包下的引用类型 -->
            <property name="forceBigDecimals" value="false"/>
        </javaTypeResolver>
        <!-- targetPackage：生成的实体类所在的包 -->
        <!-- targetProject：生成的实体类所在的硬盘位置 -->
        <javaModelGenerator targetPackage="pers.entity"
                            targetProject="src/main/java">
            <!-- 是否允许子包 -->
            <property name="enableSubPackages" value="false"/>
            <!-- 是否对modal添加构造函数 -->
            <property name="constructorBased" value="true"/>
            <!-- 是否清理从数据库中查询出的字符串左右两边的空白字符 -->
            <property name="trimStrings" value="true"/>
            <!-- 建立modal对象是否不可改变 即生成的modal对象不会有setter方法，只有构造方法 -->
            <property name="immutable" value="false"/>
        </javaModelGenerator>
        <!-- targetPackage 和 targetProject：生成的 mapper 文件的包和位置 -->
        <sqlMapGenerator targetPackage="mapper"
                         targetProject="src/main/resources">
            <!-- 针对数据库的一个配置，是否把 schema 作为字包名 -->
            <property name="enableSubPackages" value="false"/>
        </sqlMapGenerator>
        <!-- targetPackage 和 targetProject：生成的 interface 文件的包和位置 -->
        <javaClientGenerator type="XMLMAPPER"
                             targetPackage="pers.dao" targetProject="src/main/java">
        </javaClientGenerator>
        <!-- tableName是数据库中的表名，domainObjectName是生成的JAVA模型名，后面的参数不用改，要生成更多的表就在下面继续加table标签 -->
        <table tableName="user" domainObjectName="User"
               enableCountByExample="false" enableUpdateByExample="false"
               enableDeleteByExample="false" enableSelectByExample="false"
               selectByExampleQueryId="false" />
    </context>
</generatorConfiguration>
```
## 6.3 生成代码
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200929073850261.png)

双击生成即可：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200929074012121.png)

生成了实体类、`dao`接口以及`mapper`文件。

# 7 参考源码
`Java`版：

- [Github](https://github.com/2293736867/CSBookNotes/tree/master/Spring/Chapter6/JavaDemo)
- [码云](https://gitee.com/u6b7b5fc3/CSBookNotes/tree/master/Spring/Chapter6/JavaDemo)
- [CODE.CHINA](https://codechina.csdn.net/qq_27525611/CSBookNotes/-/tree/master/Spring/Chapter6/JavaDemo)

`Kotlin`版：

- [Github](https://github.com/2293736867/CSBookNotes/tree/master/Spring/Chapter6/KotlinDemo)
- [码云](https://gitee.com/u6b7b5fc3/CSBookNotes/tree/master/Spring/Chapter6/KotlinDemo)
- [CODE.CHINA](https://codechina.csdn.net/qq_27525611/CSBookNotes/-/tree/master/Spring/Chapter6/KotlinDemo)


# 8 参考链接
- [简书-IDEA使用mybatis-generator](https://www.jianshu.com/p/b519e9ef605f)
- [Github-mybatis-generator-plugin](https://github.com/Hinsteny/mybatis-generator-plugin)
