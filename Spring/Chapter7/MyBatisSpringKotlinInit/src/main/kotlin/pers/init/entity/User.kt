package pers.init.entity

class User
{
    var id = 0
    var name = ""
    var age:Short = 0

    override fun toString() = "User(id:$id\tname:$name\tage:$age)"

    companion object Builder
    {
        var user = User()
        fun name(name:String):Builder
        {
            user.name = name
            return this
        }
        fun id(id:Int):Builder
        {
            user.id = id
            return this
        }
        fun age(age:Short):Builder
        {
            user.age = age
            return this
        }
        fun build() = user
    }
}