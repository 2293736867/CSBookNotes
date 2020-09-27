package pers;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import pers.dao.TestDao;
import pers.entity.MyUser;

import java.util.List;

public class Main {
    private static ApplicationContext context;
    private static TestDao dao;
    public static void main(String[] args) {
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
//        initTest();
//        transactionAPITest();
//        transactionTemplateTest();
        transactionXMLTest();
    }

    public static void initTest()
    {
        dao = (TestDao)context.getBean("testDao");
        String insertSql = "insert into MyUser(uname,usex) values(?,?)";
        String[] param1 = {"chenhengfa1","男"};
        String[] param2 = {"chenhengfa2","男"};
        String[] param3 = {"chenhengfa3","男"};
        String[] param4 = {"chenhengfa4","男"};

        dao.update(insertSql,param1);
        dao.update(insertSql,param2);
        dao.update(insertSql,param3);
        dao.update(insertSql,param4);

        String selectSql = "select * from MyUser";
        List<MyUser> list = dao.query(selectSql,null);
        for(MyUser mu:list)
        {
            System.out.println(mu);
        }
    }

    public static void transactionAPITest()
    {
        dao = (TestDao)context.getBean("testDao");
        dao.testTransaction();
    }

    public static void transactionTemplateTest()
    {
        dao = (TestDao)context.getBean("testDao");
        dao.testTransactionTemplate();
    }

    public static void transactionXMLTest()
    {
        dao = (TestDao)context.getBean("testDao");
        dao.testXMLTransaction();
    }
}
