package pers.init.dao;

import pers.init.entity.User;
import pers.init.pojo.UserPOJO;

import java.util.List;
import java.util.Map;

public interface UserDao {
    List<User> selectAll();
    User selectById(Integer id);
    User selectByMap(Map<String,String> map);
    User selectByPOJO(UserPOJO pojo);
    int insertUser1(User user);
    int insertUser2(User user);
    int updateUser(User user);
    int deleteUser(Integer id);
    List<User> selectBySqlColumn();
    List<Map<String,Object>> selectReturnMap();
    List<UserPOJO> selectReturnPOJO();
}