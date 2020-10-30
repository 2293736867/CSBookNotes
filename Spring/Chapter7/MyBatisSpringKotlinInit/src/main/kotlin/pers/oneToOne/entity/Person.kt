package pers.oneToOne.entity

open class Person {
    var id = 0
    var name = ""
    var age:Short = 0
    var card = IdCard()
    override fun toString() = "Person(id:$id,name:$name,age:$age,card:$card)"
}