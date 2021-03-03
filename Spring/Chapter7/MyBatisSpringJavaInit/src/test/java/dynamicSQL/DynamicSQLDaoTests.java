package dynamicSQL;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import pers.dynamicSQL.DynamicSQLDao;
import pers.init.entity.User;

import java.util.List;

public class DynamicSQLDaoTests {
    private static final DynamicSQLDao dao;
    static
    {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        dao = context.getBean(DynamicSQLDao.class);
    }

    @Test
    public void testIf()
    {
        System.out.println(dao.selectByIf(User.builder().age((short) 33).name("111").build()));
    }

    @Test
    public void testChoose()
    {
        System.out.println(dao.selectByChoose(User.builder().age((short)33).build()));
    }

    @Test
    public void testTrim()
    {
        System.out.println(dao.selectByTrim(User.builder().build()));
        System.out.println(dao.selectByTrim(User.builder().name("test2").build()));
    }

    @Test
    public void testWhere()
    {
        System.out.println(dao.selectByWhere(User.builder().build()));
        System.out.println(dao.selectByWhere(User.builder().name("111").build()));
        System.out.println(dao.selectByWhere(User.builder().age((short)-3).build()));
    }

    @Test
    public void testSet()
    {
        System.out.println(dao.updateBySet(User.builder().name("999999").age((short)39).id(1).build()));
        System.out.println(dao.selectByWhere(User.builder().build()));
    }

    @Test
    public void testForeach()
    {
        System.out.println(dao.selectByForeach(List.of(1,2,3)));
    }

    @Test
    public void testBind()
    {
        System.out.println(dao.selectByBind(User.builder().name("test1").build()));
    }
}
