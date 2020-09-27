package pers

import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

fun main()
{
    val context:ApplicationContext = ClassPathXmlApplicationContext("applicationContext.xml")
}