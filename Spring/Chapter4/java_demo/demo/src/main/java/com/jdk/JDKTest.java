package com.jdk;

public class JDKTest {
    public static void test()
    {
        JDKProxy proxy = new JDKProxy();
        JDKInterface testInterface = (JDKInterface) proxy.createProxy(new JDKImpl());
        testInterface.save();
        testInterface.modify();
        testInterface.delete();
    }
}
