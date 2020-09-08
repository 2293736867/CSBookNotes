# 1 `IoC`实现方式
- `BeanFactory`
- `ApplicationContext`

# 2 

`IoC`就是创建`Bean`的权利从调用者交由`Spring`容器，实现了控制权利的反转。`DI`就是`Spring`容器将`Bean`需要的值注入到`Bean`中。

优点：

- 封装代码，由`Spring`容器管理`Bean`的实例化
- 自动初始化，由`Spring`容器负责`Bean`的初始化
- 松耦合，简化开发

# 3 
反射。

