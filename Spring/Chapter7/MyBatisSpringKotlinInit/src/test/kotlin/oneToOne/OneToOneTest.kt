package oneToOne

import org.junit.Test
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import pers.oneToOne.dao.PersonDao

class OneToOneTest
{
    @Test
    fun test()
    {
        val context = ClassPathXmlApplicationContext("applicationContext.xml")
        val dao = context.getBean(PersonDao::class.java)
        println(dao.selectPersonById1(1))
        println(dao.selectPersonById2(1))
        println(dao.selectPersonById3(1))
    }
}
