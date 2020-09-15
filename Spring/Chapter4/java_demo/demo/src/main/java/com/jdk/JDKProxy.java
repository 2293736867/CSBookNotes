package com.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JDKProxy implements InvocationHandler {
    private JDKInterface testInterface;
    public Object createProxy(JDKInterface testInterface)
    {
        this.testInterface = testInterface;
        ClassLoader loader = JDKProxy.class.getClassLoader();
        Class<?>[] classes = testInterface.getClass().getInterfaces();
        return Proxy.newProxyInstance(loader,classes,this);
    }
    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        JDKAspect aspect = new JDKAspect();
        aspect.check();
        aspect.except();
        Object obj = method.invoke(testInterface,objects);
        aspect.log();
        aspect.monitor();
        return obj;
    }
}
