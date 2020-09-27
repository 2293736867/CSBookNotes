package pers

import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.jdbc.core.StatementCallback
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.stereotype.Component
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.support.TransactionCallback
import pers.dao.TestDao
import java.lang.reflect.Executable

class Test {
    private val context:ApplicationContext = ClassPathXmlApplicationContext("applicationContext.xml")
    private val dao = context.getBean("testDao") as TestDao
    private val selectSql = "select * from MyUser"
    private var message = "执行成功"
    @Test
    fun jdbcTemplateTest()
    {
        val insertSql = "insert into MyUser(uname,usex) values(?,?)"
        val p1 = arrayOf("张三" as Any,"男" as Any)
        val p2 = arrayOf("李四" as Any,"男" as Any)

        dao.update(insertSql,p1)
        dao.update(insertSql,p2)

        val list = dao.query(selectSql)
        list.forEach { t-> println(t) }
    }

    @Test
    fun transactionAPITest()
    {
        val status:TransactionStatus = dao.manager.getTransaction(DefaultTransactionDefinition())
        try {
            val sql1 = "delete from MyUser"
            val sql2 = "insert into MyUser(id,uname,usex) values(?,?,?)"
            val p1 = arrayOf(1,"张三","男")
            dao.update(sql1)
            dao.update(sql2,p1)
            dao.update(sql2,p1)
            dao.manager.commit(status)
            println("提交")
        }
        catch (e:Exception)
        {
            e.printStackTrace()
            dao.manager.rollback(status)
            println("回滚")
        }
    }

    @Test
    fun transactionTemplateTest() {
        dao.transactionTemplate.execute {
            val deleteSql = "delete from MyUser"
            val insertSql = "insert into MyUser(id,uname,usex) values(?,?,?)"
            val p1 = arrayOf(1,"张三","男")
            try {
                dao.update(deleteSql)
                dao.update(insertSql,p1)
                dao.update(insertSql,p1)
            } catch (e:Exception) {
                message = "主键重复，业务回滚"
                e.printStackTrace()
            }
            message
        }
    }

    @Test
    fun transactionXMLTest()
    {
        dao.transactionXMLTest()
    }
}