package com.aspectj_annotation;

import org.springframework.stereotype.Component;

@Component
public class AnnotationImpl implements AnnotationInterface {
    @Override
    public void save() {
        System.out.println("保存");
    }

    @Override
    public void modify() {
        System.out.println("修改");
    }

    @Override
    public void delete() {
        System.out.println("删除");
//        注释下面这行语句开启异常通知
//        int a = 1/0;
    }
}
