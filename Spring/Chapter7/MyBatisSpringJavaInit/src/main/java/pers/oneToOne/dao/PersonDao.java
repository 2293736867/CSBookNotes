package pers.oneToOne.dao;

import pers.oneToOne.entity.Person;
import pers.oneToOne.pojo.PersonPOJO;

public interface PersonDao {
    Person selectPersonById1(Integer id);
    Person selectPersonById2(Integer id);
    PersonPOJO selectPersonById3(Integer id);
}
