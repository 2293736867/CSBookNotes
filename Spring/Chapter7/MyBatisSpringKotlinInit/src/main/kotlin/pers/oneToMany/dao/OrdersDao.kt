package pers.oneToMany.dao

import pers.oneToMany.entity.Orders

interface OrdersDao {
    fun selectOrdersById(id:Int):List<Orders>
}