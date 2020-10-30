package pers

import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import pers.init.dao.UserDao

fun main()
{
    val context:ApplicationContext = ClassPathXmlApplicationContext("applicationContext.xml")
    val dao = context.getBean("userDao") as UserDao
    println(dao.selectById(1))
}