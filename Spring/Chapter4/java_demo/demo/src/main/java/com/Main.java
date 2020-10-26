package com;

import com.aspectj_annotation.AnnotationTest;
import com.aspectj_xml.XMLTest;
import com.cglib.CGLibTest;
import com.jdk.JDKImpl;
import com.jdk.JDKInterface;
import com.jdk.JDKProxy;
import com.jdk.JDKTest;
import com.proxyclass.ProxyClassTest;

public class Main {
    public static void main(String[] args) {
//        JDKTest.test();
//        CGLibTest.test();
//        ProxyClassTest.test();
//        XMLTest.test();
        AnnotationTest.test();
    }
}
