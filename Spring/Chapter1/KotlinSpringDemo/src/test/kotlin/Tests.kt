import org.junit.Test
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

class Tests {
    @Test
    fun test()
    {
        val context:ApplicationContext = ClassPathXmlApplicationContext("applicationContext.xml")
        val test:TestInterface = context.getBean("test") as TestInterface
        test.hello()
    }
}