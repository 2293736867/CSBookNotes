package oneToMany;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import pers.oneToMany.dao.UserWithOrdersDao;

public class OneToManyTest {
    private static final UserWithOrdersDao dao;

    static {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        dao = context.getBean(UserWithOrdersDao.class);
    }

    @Test
    public void selectUserOrders()
    {
        System.out.println(dao.selectUserOrders1(1));
        System.out.println(dao.selectUserOrders2(1));
        System.out.println(dao.selectUserOrders3(1));
    }
}
