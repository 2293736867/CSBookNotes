package oneToMany

import org.junit.Test
import org.springframework.context.support.ClassPathXmlApplicationContext
import pers.oneToMany.dao.UserWithOrdersDao
import pers.oneToMany.entity.Orders

class OneToManyTest {
    @Test
    fun test()
    {
        val context = ClassPathXmlApplicationContext("applicationContext.xml")
        val dao = context.getBean(UserWithOrdersDao::class.java)
        println(dao.selectUserOrders1(1))
        println(dao.selectUserOrders2(1))
        println(dao.selectUserOrders3(1))
    }
}