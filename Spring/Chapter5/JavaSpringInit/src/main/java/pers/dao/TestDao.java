package pers.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import pers.entity.MyUser;

import java.util.List;

@Repository
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TestDao {
    private final JdbcTemplate template;
    private final DataSourceTransactionManager manager;
    private final TransactionTemplate transactionTemplate;

    private String message = "执行成功，没有事务回滚";

    public int update(String sql,Object[] args)
    {
        return template.update(sql,args);
    }

    public List<MyUser> query(String sql,Object[] args)
    {
        RowMapper<MyUser> mapper = new BeanPropertyRowMapper<>(MyUser.class);
        return template.query(sql,mapper,args);
    }

    public void testTransaction()
    {
        TransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus status = manager.getTransaction(definition);
        try
        {
            String sql1 = "delete from MyUser";
            String sql2 = "insert into MyUser(id,uname,usex) values(?,?,?)";
            Object [] param2 = {1,"张三","男"};
            template.update(sql1);
            template.update(sql2,param2);
            template.update(sql2,param2);
            manager.commit(status);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            manager.rollback(status);
            message = "主键重复，事务回滚";
        }
        System.out.println(message);
    }

    public void testTransactionTemplate()
    {
        System.out.println(transactionTemplate.execute((TransactionCallback<Object>) transactionStatus -> {
            String deleteSql = "delete from MyUser";
            String insertSql = "insert into MyUser(id,uname,usex) values(?,?,?)";
            Object[] parm = {1, "张三", "男"};
            try {
                template.update(deleteSql);
                template.update(insertSql, parm);
                template.update(insertSql, parm);
            } catch (Exception e) {
                message = "主键重复，事务回滚";
                e.printStackTrace();
            }
            return message;
        }));
    }

    public void testXMLTransaction()
    {
        String deleteSql = "delete from MyUser";
        String saveSql = "insert into MyUser(id,uname,usex) values(?,?,?)";
        Object [] parm = {1,"张三","男"};
        template.update(deleteSql);
        template.update(saveSql,parm);
        template.update(saveSql,parm);
    }
}
