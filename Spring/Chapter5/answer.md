# 1 什么是编程式事务管理？在`Spring`中有哪几种编程式事务管理

编程式事务管理就是通过手动编程的方式来实现事务管理，有两种方式：

- 基于底层`API`实现
- 基于`TransactionTemplate`实现


## 1.1 基于底层`API`实现
就是通过手动定义事务，手动开启事务的方式实现，提交需要手动提交，回滚也需要手动回滚，一般在捕捉到异常时进行手动回滚，没有的话就手动提交。
## 1.2 基于`TranscationTemplate`实现
通过其中的`execute`方法，传入一个`TransactionCallback`类型的参数，在`doInsaction`编写业务代码，可以根据默认规则进行回滚，或手动调用`TransactionStatus`的方法进行回滚。

# 2 简述声明式事务管理的处理方式

主要有两种方式：

- 基于`XML`实现
- 基于注解实现

## 2.1 基于`XML`实现
基于`XML`实现的话需要提供：

- `<tx:advice>`
- `<tx:method>`
- `<aop:config>`

表示配置事务的通知，指定执行事务的细节以及`AOP`的配置，然后在对应方法上编写业务代码即可，出现异常会根据事务管理器进行回滚。

## 2.2 基于注解实现
核心就是`@Transactional`注解，一般作用于业务类上，在配置文件加上：

```xml
<tx:annotation-driven transaction-manager>
```
指定注解驱动器以及其中的事务管理器后即可。
