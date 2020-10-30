package pers.init.pojo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class UserPOJO {
    private Integer id;
    private String name;
    private Integer age;
}
