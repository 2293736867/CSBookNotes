package dynamicSQL

import org.junit.Test
import org.springframework.context.support.ClassPathXmlApplicationContext
import pers.dynamicSQL.DynamicSQLDao
import pers.init.entity.User

class DynamicSQLTest {
    companion object {
        private val context = ClassPathXmlApplicationContext("applicationContext.xml")
        val dao: DynamicSQLDao = context.getBean(DynamicSQLDao::class.java)
    }

    @Test
    fun testIf() = println(dao.selectByIf(User.Builder.age(33).name("111").build()))

    @Test
    fun testChoose() = println(dao.selectByChoose(User.Builder.age(33).build()))

    @Test
    fun testTrim()
    {
        println(dao.selectByTrim(User()))
        println(dao.selectByTrim(User.Builder.name("test2").build()))
    }

    @Test
    fun testWhere()
    {
        println(dao.selectByWhere(User()))
        println(dao.selectByWhere(User.Builder.name("111").build()))
        println(dao.selectByWhere(User.Builder.age(-3).build()))
    }

    @Test
    fun testSet()
    {
        println(dao.updateBySet(User.Builder.name("999999").age(39).id(1).build()))
        println(dao.selectByWhere(User.Builder.build()))
    }

    @Test
    fun testForeach() = println(dao.selectByForeach(listOf(1,2,3)))

    @Test
    fun testBind() = println(dao.selectByBind(User.Builder.name("test1").build()))
}