package com.aspectj_xml;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class XMLTest {
    public static void test()
    {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        XMLInterface xmlInterface = (XMLInterface)context.getBean("xmlImpl");
        xmlInterface.delete();
        xmlInterface.modify();
        xmlInterface.save();
    }
}
