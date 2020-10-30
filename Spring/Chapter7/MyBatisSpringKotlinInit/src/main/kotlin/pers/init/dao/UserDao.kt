package pers.init.dao

import pers.init.entity.User
import pers.init.pojo.UserPOJO

interface UserDao {
    fun selectAll():List<User>
    fun selectById(id:Int):User
    fun selectByMap(map:Map<String,String>):User
    fun selectByPOJO(pojo: UserPOJO)
    fun insertUser1(user:User):Int
    fun insertUser2(user:User):Int
    fun updateUser(user:User):Int
    fun deleteUser(id:Int):Int
    fun selectBySqlColumn():List<User>
    fun selectReturnMap():List<Map<String,Any>>
    fun selectReturnPOJO():List<UserPOJO>
}
