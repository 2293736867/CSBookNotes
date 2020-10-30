package manyToMany;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import pers.manyToMany.dao.OrdersWithProductDao;

public class ManyToManyTest {
    @Test
    public void test()
    {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        OrdersWithProductDao dao = context.getBean(OrdersWithProductDao.class);
        System.out.println(dao.selectOrdersAndProduct());
    }
}
