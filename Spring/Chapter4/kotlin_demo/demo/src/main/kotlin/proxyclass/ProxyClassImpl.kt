package proxyclass

open class ProxyClassImpl : ProxyClassInterface{
    override fun save() {
        println("保存")
    }

    override fun modify() {
        println("修改")
    }

    override fun delete() {
        println("删除")
    }
}