package pers.oneToMany.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWithOrders {
    private Integer id;
    private String name;
    private Short age;
    private List<Orders> ordersList;
}