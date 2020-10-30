package pers.oneToOne.dao

import pers.oneToOne.entity.IdCard

interface IdCardDao {
    fun selectCodeById(id:Int): IdCard
}