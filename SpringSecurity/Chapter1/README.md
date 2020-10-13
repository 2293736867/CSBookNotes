# 1 `Spring Security`
## 1.1 简介
`Spring Security`是一个强大和高度可定制的认证和控制访问框架，是基于`Spring`应用的事实上的安全标准，主要聚焦于为`Java`应用提供授权和认证功能，主要功能如下：

- 对认证和授权的全面和可扩展支持
- 防止诸如`Session Fixation`、点击劫持、跨站点请求伪造攻击
- `Servlet API`集成
- `Spring MVC`可选集成


## 1.2 已集成的认证技术
- `HTTP BASIC authentication headers`/`HTTP Digest authentication headers`/`HTTP X.509 client certificate exchange`：基于`IETF RFC`的标准
- `LDAP`：常见的跨平台身份验证方式
- `Form-based authentication`：用于简单的用户界面需求
- `OpenID authentication`：一种去中心化的身份认证方式
- `Authentication based on pre-established request headers`：一种用户身份验证以及授权的集中式安全基础方案
- `Jasig Central Authentication Service`：单点登录方案
- `Transparent authentication context propagation for Remote Method Invocation and HttpInvoker`：一个`Spring`远程调用协议
- `Automatic "remember-me" authentication`：允许在指定到期时间前自行重新登录系统
- `Anonymous authentication`：允许匿名用户使用特定身份安全访问资源
- `Run-as authentication`：允许在一个会话中变换用户身份的机制
- `Java Authentication and Authorization`：`JASS`，`Java`验证和授权`API`
- `Java EE container authentication`：允许系统继续使用容器管理这种身份验证方式
- `Kerberos`：一种使用对称密钥机制，允许客户端和服务器相互确认身份的认证协议


除此之外还引入了其他第三方包，比如`JOSSO`，另外如果都无法满足需求，`Spring Security`也允许开发人员自己编写认证技术。


# 2 简单`Demo`
下面来动手做一个简单的`Demo`去体验一下`Spring Security`。

## 2.1 新建工程
![在这里插入图片描述](https://img-blog.csdnimg.cn/20201012075800260.png)

默认即可：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201012075824621.png)

依赖选择`Spring Web`和`Spring Secutiry`：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201013073435791.png)


## 2.2 启动类
修改启动类，使其作为`Controller`类：
```java
@SpringBootApplication
@RestController
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping("/")
    public String hello(){
        return "Hello, Spring Security.";
    }
}
```

## 2.3 运行
运行后可以看见控制台有一串自动生成的密码：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201013073732646.png)

浏览器访问
```bash
http://localhost:8080
```
会自动跳转到
```bash
http://localhost:8080/login
```

密码输入控制台中的密码，用户名默认为`user`，就可以登录了：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201013074024977.png)

成功后会自动跳转到
```bash
http://localhost:8080
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201013074042400.png)


## 2.3 修改基本认证用户名与密码
修改`application.properties`/`application.yaml`，添加如下属性：
```bash
spring.security.user.name=test
spring.security.user.password=test
```
该认证方式实质上是`HTTP`基本认证方式，只需要用户名和密码，而上面的两项属性可以自定义基本认证的用户名和密码，测试：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20201013074624172.png)

# 3 参考
- [Spring Security官网](https://spring.io/projects/spring-security)
