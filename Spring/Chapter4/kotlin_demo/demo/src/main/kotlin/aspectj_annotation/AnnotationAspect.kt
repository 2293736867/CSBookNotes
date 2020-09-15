package aspectj_annotation

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.*
import org.springframework.stereotype.Component

@Aspect
@Component
class AnnotationAspect
{
    @Pointcut("execution(* aspectj_annotation.AnnotationInterface.*(..))")
    fun pointcut(){}

    @Before("pointcut()")
    fun before(joinPoint: JoinPoint){
        println("前置通知")
        println(joinPoint.signature)
    }

    @AfterReturning("pointcut()")
    fun afterReturning(joinPoint: JoinPoint){
        println("后置返回通知")
        println(joinPoint.args.size)
    }

    @Around("pointcut()")
    fun around(proceedingJoinPoint: ProceedingJoinPoint):Any?
    {
        println("环绕通知开始")
        val obj = proceedingJoinPoint.proceed()
        println("环绕通知结束")
        return obj
    }

    @AfterThrowing(value = "pointcut()",throwing = "e")
    fun expect(e:Throwable) = println("异常通知")

    @After("pointcut()")
    fun after() = println("后置最终通知")
}