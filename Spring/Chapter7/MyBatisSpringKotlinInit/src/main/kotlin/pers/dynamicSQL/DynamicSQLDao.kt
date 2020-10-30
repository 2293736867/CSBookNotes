package pers.dynamicSQL

import pers.init.entity.User

interface DynamicSQLDao {
    fun selectByIf(user:User):List<User>
    fun selectByChoose(user:User):List<User>
    fun selectByTrim(user:User):List<User>
    fun selectByWhere(user:User):List<User>
    fun updateBySet(user:User):Int
    fun selectByForeach(id:List<Int>):List<User>
    fun selectByBind(user:User):List<User>
}