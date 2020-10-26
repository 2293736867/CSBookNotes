package pers.entity

class User {

    var id = 0
    var name = ""

    constructor()
    {
        id = 0
    }
    constructor(name:String):this()
    {
        this.name = name
    }
    override fun toString() = "id:$id\tname:$name"
}