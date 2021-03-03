package pers.init.pojo

class UserPOJO(){
    var id = 0
    var name = ""
    var age:Short = 1

    constructor(name:String,age:Short):this()
    {
        this.name = name
        this.age = age
    }

    //MyBatis需要使用该构造方法
    @Suppress("unused")
    constructor(id:Int,name: String,age: Short):this(name,age)
    {
        this.id = id
    }

    override fun toString() = "UserPOJO(id:$id,name:$name,age:$age)"
}