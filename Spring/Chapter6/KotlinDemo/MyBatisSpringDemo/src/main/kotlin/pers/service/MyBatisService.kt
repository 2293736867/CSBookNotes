package pers.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pers.dao.UserDao
import pers.entity.User


@Service
@Transactional
open class MyBatisService(@Autowired private var dao:UserDao)
{
    open fun test()
    {
        println(dao.selectById(13))
        dao.insert(User("333"))
        dao.selectAll().forEach { t-> println(t) }
        dao.delete(12)
        dao.selectAll().forEach { t-> println(t) }
    }
}