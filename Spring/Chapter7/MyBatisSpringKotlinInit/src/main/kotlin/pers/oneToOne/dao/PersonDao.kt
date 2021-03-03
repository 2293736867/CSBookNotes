package pers.oneToOne.dao

import pers.oneToOne.entity.Person
import pers.oneToOne.pojo.PersonPOJO

interface PersonDao {
    fun selectPersonById1(id:Int):Person
    fun selectPersonById2(id:Int):Person
    fun selectPersonById3(id:Int):PersonPOJO
}