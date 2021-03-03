package pers.manyToMany.entity

class OrdersWithProduct {
    var id = 0
    var ordersn = ""
    var products:List<Product> = mutableListOf()
    override fun toString() = "OrdersWithProduct(id:$id,ordersn:$ordersn,products:$products)"
}