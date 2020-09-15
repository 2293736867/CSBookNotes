package com.proxyclass;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ProxyClassTest {
    public static void test()
    {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        ProxyInterface proxyInterface = (ProxyInterface)context.getBean("factory");
        proxyInterface.delete();
        proxyInterface.save();
        proxyInterface.modify();
    }
}
