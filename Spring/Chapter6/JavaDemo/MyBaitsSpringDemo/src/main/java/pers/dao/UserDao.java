package pers.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import pers.entity.User;

import java.util.List;

@Repository
@Mapper
public interface UserDao {
    User selectById(Integer id);
    List<User> selectAll();
    int insert(User user);
    int update(User user);
    int delete(Integer id);
}
