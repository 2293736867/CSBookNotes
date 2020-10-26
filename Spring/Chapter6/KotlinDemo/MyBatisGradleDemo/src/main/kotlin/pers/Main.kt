package pers

import org.apache.ibatis.io.Resources
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import pers.entity.User

fun main()
{
    try
    {
        val inputStream = Resources.getResourceAsStream("config/mybatis-config.xml")
        val session = SqlSessionFactoryBuilder().build(inputStream).openSession()
        val user = session.selectOne<User>("UserMapper.selectById",13)
        println(user)
        val user1 = User("test")
        session.insert("UserMapper.insert",user1)
        user1.name = "User1name"
        session.update("UserMapper.update",user1)
        val list = session.selectList<User>("UserMapper.selectAll")
        list.forEach { t-> println(t) }
        session.commit()
        session.close()
    }
    catch (e:Exception)
    {
        e.printStackTrace()
    }
}