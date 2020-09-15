package com.cglib;

public class CGLibTest {
    public static void test() {
        CGLibProxy proxy = new CGLibProxy();
        CGLibTestInterface cgLibTestInterface = (CGLibTestInterface) proxy.createProxy(new CGLibImpl());
        cgLibTestInterface.delete();
        cgLibTestInterface.modify();
        cgLibTestInterface.save();
    }
}
