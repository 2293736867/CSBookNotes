package pers.manyToMany.entity

import pers.oneToMany.entity.Orders

class Product {
    var id = 0
    var name = ""
    var price = 0.0
    var orders = mutableListOf<Orders>()
    override fun toString() = "Product(id:$id,name:$name,price:$price,orders:$orders)"
}