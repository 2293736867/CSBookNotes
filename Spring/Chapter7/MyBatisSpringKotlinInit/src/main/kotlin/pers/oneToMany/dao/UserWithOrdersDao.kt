package pers.oneToMany.dao

import pers.oneToMany.entity.UserWithOrders
import pers.oneToMany.pojo.UserOrdersPOJO

interface UserWithOrdersDao {
    fun selectUserOrders1(id:Int):UserWithOrders
    fun selectUserOrders2(id:Int):UserWithOrders
    fun selectUserOrders3(id:Int):List<UserOrdersPOJO>
}