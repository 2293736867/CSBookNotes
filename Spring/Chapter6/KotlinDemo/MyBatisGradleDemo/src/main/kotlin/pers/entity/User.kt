package pers.entity

class User {
    var id = 0
    var name = ""
    constructor(id:Int,name:String)
    {
        this.id = id
        this.name = name
    }
    constructor(name:String)
    {
        this.name = name
    }
    override fun toString() = "id:$id\tname:$name"
}