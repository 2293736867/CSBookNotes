import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

fun main() {
    println("Hello")
    val context: ApplicationContext = ClassPathXmlApplicationContext("applicationContext.xml")
    val test: TestInterface = context.getBean("test") as TestInterface
    test.hello()
}