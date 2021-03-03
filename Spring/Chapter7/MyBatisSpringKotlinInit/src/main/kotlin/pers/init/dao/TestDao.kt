package pers.init.dao

import pers.init.entity.User

interface TestDao {
    fun selectByPrimaryKey(id:Int): User
}