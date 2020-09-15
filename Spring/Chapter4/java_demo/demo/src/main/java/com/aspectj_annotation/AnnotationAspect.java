package com.aspectj_annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AnnotationAspect {

    @Pointcut("execution(* com.aspectj_annotation.AnnotationInterface.*(..))")
    public void pointcut(){}

    @Before("pointcut()")
    public void before()
    {
        System.out.println("前置通知");
    }

    @AfterReturning(value = "pointcut()")
    public void afterReturning()
    {
        System.out.println("后置返回通知");
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        System.out.println("环绕通知开始");
        Object object = proceedingJoinPoint.proceed();
        System.out.println("环绕通知结束");
        return object;
    }

    @AfterThrowing(value = "pointcut()",throwing = "e")
    public void except(Throwable e)
    {
        System.out.println("异常通知");
    }

    @After("pointcut()")
    public void after()
    {
        System.out.println("后置最终通知");
    }

}
