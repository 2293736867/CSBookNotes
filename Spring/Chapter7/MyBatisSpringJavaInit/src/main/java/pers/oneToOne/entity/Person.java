package pers.oneToOne.entity;

import lombok.Data;

@Data
public class Person {
    private Integer id;
    private String name;
    private Short age;
    private IdCard card;
}
