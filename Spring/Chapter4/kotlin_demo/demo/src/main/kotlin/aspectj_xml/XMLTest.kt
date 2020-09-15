package aspectj_xml

import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

fun test()
{
    val context:ApplicationContext = ClassPathXmlApplicationContext("applicationContext.xml")
    val xmlInterface = context.getBean("xmlImpl") as XMLInterface
    xmlInterface.delete()
    xmlInterface.modify()
    xmlInterface.save()
}