package com.aspectj_annotation;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AnnotationTest {
    public static void test()
    {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        AnnotationInterface annotationInterface = (AnnotationInterface)context.getBean("annotationImpl");
        annotationInterface.delete();
        annotationInterface.modify();
        annotationInterface.save();
    }
}
