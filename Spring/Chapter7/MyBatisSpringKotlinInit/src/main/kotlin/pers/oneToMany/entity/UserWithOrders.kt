package pers.oneToMany.entity

open class UserWithOrders {
    var id = 0
    var name = ""
    var age:Short = 0
    var ordersList:List<Orders> = mutableListOf()
    override fun toString() = "UserWithOrders(id:$id,name:$name,age:$age,ordersList:$ordersList)"
}