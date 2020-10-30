package oneToOne;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import pers.oneToOne.dao.PersonDao;

public class OneToOneTest {
    private static final PersonDao dao;
    static {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        dao = context.getBean(PersonDao.class);
    }

    @Test
    public void selectPersonById()
    {
        System.out.println(dao.selectPersonById1(1));
        System.out.println(dao.selectPersonById2(1));
        System.out.println(dao.selectPersonById3(1));
    }
}
