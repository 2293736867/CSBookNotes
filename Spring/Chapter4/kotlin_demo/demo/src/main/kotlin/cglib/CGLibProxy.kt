package cglib

import net.sf.cglib.proxy.Enhancer
import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import java.lang.reflect.Method

class CGLibProxy : MethodInterceptor{

    fun createProxy(target: Any):Any
    {
        val enhancer = Enhancer()
        enhancer.setSuperclass(target.javaClass)
        enhancer.setCallback(this)
        return enhancer.create()
    }

    override fun intercept(obj: Any?, method: Method?, args: Array<out Any>?, proxy: MethodProxy?): Any?{
        val aspect = CGLibAspect()
        aspect.check()
        aspect.expect()
        val o = proxy?.invokeSuper(obj,args)
        aspect.log()
        aspect.monitor()
        return o
    }
}