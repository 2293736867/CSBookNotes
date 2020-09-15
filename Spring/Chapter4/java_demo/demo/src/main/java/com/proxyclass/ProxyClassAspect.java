package com.proxyclass;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class ProxyClassAspect implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        check();
        expect();
        Object obj = methodInvocation.proceed();
        log();
        monitor();
        return obj;
    }

    private void check()
    {
        System.out.println("模拟权限控制");
    }

    private void expect()
    {
        System.out.println("模拟异常处理");
    }

    private void log()
    {
        System.out.println("模拟日志记录");
    }

    private void monitor()
    {
        System.out.println("性能监测");
    }
}
