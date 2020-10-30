# 参考解答
# 1 
使用`POJO`/`Map`存储，通常结合`resultType`/`resultMap`指定。

## 1.1 `resultType`
`resultType`默认`POJO`形式，`POJO`类属性名与表字段名一一对应，使用时在`<select>`中的`resultType`填上`POJO`全限定类名即可，返回结果可以用`List<POJO_CLASS>`接收。

## 1.2 `resultMap`
而`resultMap`可以使用`Map`或`POJO`形式存储，使用`Map`时直接在`<select>`的`resultMap`中指定为`map`，返回结果可以用`List<Map>`接收

（`resultType`可以支持`Map`?）
# 2 
- 针对`MySQL`、`SQL Server`等数据库可以采用自动递增的主键，可以加上`keyProperty`属性指定回填的`POJO`属性
- 对于不支持自增主键的`DBMS`，可以采用`<selectKey>`自定义生成主键，一般需要设置`<selectKey>`的`order`为`BEFORE`，在元素中定义对应的生成主键的语句即可
# 3 
- 使用`Map`传递：`parameterType`中填写`map`
- 使用`JavaBean`传递：`parameterType`中填写对应的`JavaBean`全限定类名
# 4
在进行模糊查询时，一般需要用到`<bind>`，用`<bind>`可以防止`SQL`注入，而且可以屏蔽`DBMS`之间的连接符或连接函数的差别。
