package jdk

fun test()
{
    val proxy = JDKProxy()
    val jdkInterface = proxy.createProxy(JDKImpl()) as JDKInterface
    jdkInterface.save()
    jdkInterface.modify()
    jdkInterface.delete()
}