package proxyclass

import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation

class ProxyClassAspect:MethodInterceptor{
    override fun invoke(invocation: MethodInvocation?): Any? {
        check()
        expect()
        val obj = invocation?.proceed()
        log()
        monitor()
        return obj
    }

    private fun check() = println("模拟权限控制")

    private fun expect() = println("模拟异常处理")

    private fun log() = println("模拟日志记录")

    private fun monitor() = println("性能监测")
}