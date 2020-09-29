package pers.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class User {
    private Integer id;
    private String name;

    @Override
    public String toString() {
        return "id:"+id+"\tname:"+name;
    }
}

