package pers.manyToMany.dao

import pers.manyToMany.entity.OrdersWithProduct

interface OrdersWithProductDao {
    fun selectOrdersAndProduct():List<OrdersWithProduct>
}