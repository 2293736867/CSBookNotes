package pers.dao

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import pers.entity.MyUser

@Repository
@Transactional
open class TestDao{

    @Autowired
    lateinit var template: JdbcTemplate
    @Autowired
    lateinit var manager: DataSourceTransactionManager
    @Autowired
    lateinit var transactionTemplate: TransactionTemplate

    var message="执行成功，没有事务回滚"

    fun update(sql:String) = template.update(sql)

    fun update(sql:String, args: Array<Any>) = template.update(sql,*args)

    fun query(sql:String) = template.query(sql,BeanPropertyRowMapper<MyUser>(MyUser::class.java))

    open fun transactionXMLTest() {
        val deleteSql = "delete from MyUser"
        val insertSql = "insert into MyUser(id,uname,usex) values(?,?,?)"
        val p1 = arrayOf(1, "张三", "男")
        template.update(deleteSql)
        template.update(insertSql,*p1)
        template.update(insertSql,*p1)
    }
}