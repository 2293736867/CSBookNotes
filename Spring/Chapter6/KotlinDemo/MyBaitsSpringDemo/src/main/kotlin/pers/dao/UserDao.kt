package pers.dao

import org.apache.ibatis.annotations.Mapper
import org.springframework.stereotype.Repository
import pers.entity.User

@Mapper
@Repository
interface UserDao {
    fun selectById(id:Int):User
    fun selectAll():List<User>
    fun insert(user: User):Int
    fun update(user: User):Int
    fun delete(id: Int):Int
}