package jdk

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class JDKProxy:InvocationHandler{
    lateinit var jdkInterface:JDKInterface

    fun createProxy(jdkInterface: JDKInterface): Any {
        this.jdkInterface = jdkInterface
        val loader:ClassLoader = JDKProxy::class.java.classLoader
        val classes:Array<Class<*>> = jdkInterface.javaClass.interfaces
        return Proxy.newProxyInstance(loader,classes,this)
    }

    override fun invoke(p0: Any?, p1: Method?, p2: Array<out Any>?): Any? {
        val aspect = JDKAspect()
        aspect.check()
        aspect.expect()
        val obj:Any? = p1?.invoke(jdkInterface,*(p2?: arrayOfNulls<Any>(0)))
        aspect.log()
        aspect.monitor()
        return obj
    }
}