package pers.manyToMany.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdersWithProduct {
    private Integer id;
    private String ordersn;
    private List<Product> products;
}
