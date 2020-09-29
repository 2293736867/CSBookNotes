package pers;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import pers.entity.User;

import java.io.InputStream;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try
        {
            InputStream inputStream = Resources.getResourceAsStream("config/mybatis-config.xml");
            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(inputStream);
            SqlSession session = factory.openSession();
            User user = session.selectOne("UserMapper.selectById",1);
            System.out.println(user);
            User user1 = User.builder().name("test").build();
            session.insert("UserMapper.insert",user1);
            user1.setName("222");
            session.update("UserMapper.update",user1);
            List<User> list = session.selectList("UserMapper.selectAll");
            list.forEach(System.out::println);
            session.delete("UserMapper.delete",1);
            session.commit();
            session.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
