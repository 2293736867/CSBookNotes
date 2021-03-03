package init;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import pers.init.dao.UserDao;
import pers.init.entity.User;
import pers.init.pojo.UserPOJO;

import java.util.HashMap;
import java.util.Map;

public class InitTest {
    private static final UserDao dao;
    static {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        dao = context.getBean(UserDao.class);
    }

    @Test
    public void selectAll()
    {
        dao.selectAll().forEach(System.out::println);
    }

    @Test
    public void selectById()
    {
        System.out.println(dao.selectById(1));
    }

    @Test
    public void selectByMap()
    {
        Map<String,String> map = new HashMap<>();
        map.put("name","111");
        map.put("age","33");
        System.out.println(dao.selectByMap(map));
    }

    @Test
    public void selectByPOJO()
    {
        UserPOJO pojo = UserPOJO.builder().age(33).name("111").build();
        System.out.println(dao.selectByPOJO(pojo));
    }

    @Test
    public void insertUser1()
    {
        User user = User.builder().age((short) 88).name("test1").build();
        System.out.println(dao.insertUser1(user));
        System.out.println(user.getId());
    }

    @Test
    public void insertUser2()
    {
        User user = User.builder().age((short) 10).name("test2").build();
        System.out.println(dao.insertUser2(user));
        System.out.println(user.getId());
    }

    @Test
    public void updateUser()
    {
        User user = User.builder().id(3).name("3333333").age((short)11).build();
        selectAll();
        System.out.println(dao.updateUser(user));
        selectAll();
    }

    @Test
    public void deleteUser()
    {
        selectAll();
        System.out.println(dao.deleteUser(3));
        selectAll();
    }

    @Test
    public void selectBySqlColumn()
    {
        System.out.println(dao.selectBySqlColumn());
    }

    @Test
    public void selectReturnMap()
    {
        dao.selectReturnMap().forEach(System.out::println);
    }

    @Test
    public void selectReturnPOJO()
    {
        dao.selectReturnPOJO().forEach(System.out::println);
    }
}
