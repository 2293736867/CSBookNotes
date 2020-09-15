package aspectj_xml

import org.aspectj.lang.ProceedingJoinPoint
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint

class XMLAspect {
    fun before() = println("前置通知")

    fun afterReturning() = println("后置返回通知")

    fun around(proceedingJoinPoint: ProceedingJoinPoint):Any?
    {
        println("环绕通知开始")
        val obj = proceedingJoinPoint.proceed()
        println("环绕通知结束")
        return obj
    }

    fun expect(e:Throwable)
    {
        println("异常通知")
    }

    fun after()
    {
        println("后置返回通知")
    }
}