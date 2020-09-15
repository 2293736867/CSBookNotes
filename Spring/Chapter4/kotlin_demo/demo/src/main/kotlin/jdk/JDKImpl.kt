package jdk

class JDKImpl : JDKInterface{
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