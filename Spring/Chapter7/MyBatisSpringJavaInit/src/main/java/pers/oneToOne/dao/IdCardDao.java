package pers.oneToOne.dao;

import pers.oneToOne.entity.IdCard;

public interface IdCardDao {
    IdCard selectCodeById(Integer id);
}
