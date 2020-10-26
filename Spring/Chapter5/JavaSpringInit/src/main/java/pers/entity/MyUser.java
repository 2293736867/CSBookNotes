package pers.entity;

import lombok.Setter;

@Setter
public class MyUser {
    private Integer id;
    private String uname;
    private String usex;

    @Override
    public String toString() {
        return "id:"+id+"\tuname:"+uname+"\tusex:"+usex;
    }
}
