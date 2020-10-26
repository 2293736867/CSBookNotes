package com.cglib;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class CGLibProxy implements MethodInterceptor {
    public Object createProxy(Object target)
    {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target.getClass());
        enhancer.setCallback(this);
        return enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        CGLibAspect aspect = new CGLibAspect();
        aspect.check();
        aspect.except();
        Object obj = methodProxy.invokeSuper(o,objects);
        aspect.log();
        aspect.monitor();
        return obj;
    }
}
