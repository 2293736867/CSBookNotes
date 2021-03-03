package pers.oneToMany.dao;

import pers.oneToMany.entity.Orders;

import java.util.List;

public interface OrdersDao {
    List<Orders> selectOrdersById(Integer id);
}
