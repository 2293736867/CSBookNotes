package pers.init.dao;

import pers.init.entity.User;

public interface TestDao {
    User selectByPrimaryKey(Integer id);
}