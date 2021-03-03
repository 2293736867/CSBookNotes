package pers.oneToMany.dao;

import pers.oneToMany.pojo.UserOrdersPOJO;
import pers.oneToMany.entity.UserWithOrders;

import java.util.List;

public interface UserWithOrdersDao {
    UserWithOrders selectUserOrders1(Integer id);
    UserWithOrders selectUserOrders2(Integer id);
    List<UserOrdersPOJO> selectUserOrders3(Integer id);
}
