package manyToMany

import org.junit.Test
import org.springframework.context.support.ClassPathXmlApplicationContext
import pers.manyToMany.dao.OrdersWithProductDao

class ManyToManyTest {
    @Test
    fun test()
    {
        val context = ClassPathXmlApplicationContext("applicationContext.xml")
        val dao = context.getBean(OrdersWithProductDao::class.java)
        println(dao.selectOrdersAndProduct())
    }
}