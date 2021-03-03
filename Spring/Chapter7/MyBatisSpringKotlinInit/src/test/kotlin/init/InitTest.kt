package init

import org.junit.Test
import org.springframework.context.support.ClassPathXmlApplicationContext
import pers.init.dao.UserDao
import pers.init.entity.User
import pers.init.pojo.UserPOJO

class InitTest {
    companion object Dao
    {
        val dao = ClassPathXmlApplicationContext("applicationContext.xml").getBean(UserDao::class.java) as UserDao
    }

    @Test
    fun selectAll() = println(dao.selectAll())

    @Test
    fun selectById() = println(dao.selectById(1))

    @Test
    fun selectByMap() = println(dao.selectByMap(mapOf(Pair("name","111"), Pair("age","33"))))

    @Test
    fun selectByPOJO() = println(dao.selectByPOJO(UserPOJO("111",33)))

    @Test
    fun insertUser1()
    {
        val user = User.Builder.name("test1").age(88).build()
        println(dao.insertUser1(user))
        println(user.id)
    }

    @Test
    fun insertUser2()
    {
        val user = User.Builder.name("test2").age(10).build()
        println(dao.insertUser2(user))
        println(user.id)
    }

    @Test
    fun updateUser()
    {
        val user = User.Builder.id(1).name("3333333333").age(36).build()
        selectAll()
        println(dao.updateUser(user))
        selectAll()
    }

    @Test
    fun deleteUser()
    {
        selectAll()
        println(dao.deleteUser(3))
        selectAll()
    }

    @Test
    fun selectBySqlColumn() = println(dao.selectBySqlColumn())

    @Test
    fun selectReturnMap() = dao.selectReturnMap().forEach{ println("$it") }

    @Test
    fun selectReturnPOJO() = dao.selectReturnPOJO().forEach{ println("$it") }
}