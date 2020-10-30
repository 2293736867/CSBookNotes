# 该`Kotlin`工程需要注意的一些问题

- 如果类属性使用了`List`，请使用`mutableListOf()`初始化而不是`emptyList()`初始化，否则提示操作只读错误
- 某些实体类需要手动加上`open`，否则会提示该类为`final`
- 构造方法需要手动定义（~~当然可以试一下`@AllArgsConstructor`/`@NoArgsConstructor`~~）
- 没有`@Builder`，需要手动定义伴生对象`Builder`
- 没有`@ToString`，需要手动`override fun toString()`
- `Kotlin`不知道为什么不能使用`Java`中的`org.junit.jupiter.api.Test`，而是使用`org.junit.Test`，两者看上起好像差不多，但是从`Maven`中心仓库中可以看到`org.junit.jupiter`已经迁移到`junit-jupiter-api`
