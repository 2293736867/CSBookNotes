import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import pers.init.dao.TestDao;
import pers.init.dao.UserDao;

public class MainTest {

    @Test
    public void test()
    {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        TestDao dao = (TestDao) context.getBean("testDao");
        System.out.println(dao.selectByPrimaryKey(1));
        UserDao userDao = (UserDao) context.getBean("userDao");
        System.out.println(userDao.selectById(1));
    }

}
