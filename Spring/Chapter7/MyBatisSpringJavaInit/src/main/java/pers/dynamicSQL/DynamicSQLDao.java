package pers.dynamicSQL;

import pers.init.entity.User;
import pers.init.pojo.UserPOJO;

import java.util.List;

public interface DynamicSQLDao {
    List<User> selectByIf(User user);
    List<User> selectByChoose(User user);
    List<User> selectByTrim(User user);
    List<User> selectByWhere(User user);
    int updateBySet(User user);
    List<User> selectByForeach(List<Integer> id);
    List<User> selectByBind(User user);
}
