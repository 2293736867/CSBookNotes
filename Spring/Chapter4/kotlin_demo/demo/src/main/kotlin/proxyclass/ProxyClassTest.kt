package proxyclass

import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

fun test()
{
    val context:ApplicationContext = ClassPathXmlApplicationContext("applicationContext.xml")
    val proxyClassInterface = context.getBean("factory") as ProxyClassInterface
    proxyClassInterface.delete()
    proxyClassInterface.save()
    proxyClassInterface.modify()
}