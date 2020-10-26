package pers.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pers.dao.UserDao;
import pers.entity.User;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MyBatisService {
    private final UserDao dao;

    public void test(){
        User user = dao.selectById(13);
        System.out.println(user);
        dao.insert(User.builder().name("333").build());
        dao.update(User.builder().name("88888").id(13).build());
        dao.selectAll().forEach(System.out::println);
        dao.delete(12);
        dao.selectAll().forEach(System.out::println);
    }
}
