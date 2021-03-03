import org.junit.Test
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import pers.init.dao.TestDao

class MainTest {
    @Test
    fun test()
    {
        val context: ApplicationContext = ClassPathXmlApplicationContext("applicationContext.xml")
        val dao = context.getBean("testDao") as TestDao
        println(dao.selectByPrimaryKey(1))
    }
}