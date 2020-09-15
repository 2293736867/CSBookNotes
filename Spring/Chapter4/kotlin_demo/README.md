注意的点：

- 1、使用自带`JDK`实现时，`invoke`方法的调用形式为`invoke(jdkInterface,*(p2?:arrayOfNulls<Any>(0)))`
- 2、关于使用`AspectJ`注解实现的`AOP`问题，在`before`方法可以加上`Joinpoint`参数，但相同的配置下`Java`却不可以，因为`Java`中选择的是`org.aopalliance.intercept.Joinpoint`，而在`Kotlin`中选择的是`org.aspectj.lang.Joinpoint`。但问题是`Java`中选择`org.aspectj.lang.Joinpoint`的话`IDEA`会报错
