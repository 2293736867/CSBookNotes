package cglib

fun test()
{
    val proxy = CGLibProxy()
    val cgLibInterface = proxy.createProxy(CGLibImpl()) as CGLibInterface
    cgLibInterface.delete()
    cgLibInterface.modify()
    cgLibInterface.save()
}