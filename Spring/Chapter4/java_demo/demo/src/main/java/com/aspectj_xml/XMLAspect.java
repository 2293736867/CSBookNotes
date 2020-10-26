package com.aspectj_xml;

import org.aopalliance.intercept.Joinpoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Before;

public class XMLAspect {
    public void before()
    {
        System.out.println("前置通知");
    }

    public void afterReturning()
    {
        System.out.println("后置返回通知");
    }

    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        System.out.println("环绕通知开始");
        Object object = proceedingJoinPoint.proceed();
        System.out.println("环绕通知结束");
        return object;
    }

    public void expect(Throwable e)
    {
        System.out.println("异常通知");
    }

    public void after()
    {
        System.out.println("后置最终通知");
    }
}
