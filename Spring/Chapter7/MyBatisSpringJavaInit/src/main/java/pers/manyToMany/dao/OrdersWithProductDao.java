package pers.manyToMany.dao;

import pers.manyToMany.entity.OrdersWithProduct;

import java.util.List;

public interface OrdersWithProductDao {
    List<OrdersWithProduct> selectOrdersAndProduct();
}
