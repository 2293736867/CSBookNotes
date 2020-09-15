package aspectj_annotation

import org.springframework.stereotype.Component

@Component
class AnnotationImpl : AnnotationInterface{
    override fun save() {
        println("保存")
    }

    override fun modify() {
        println("修改")
    }

    override fun delete() {
        println("删除")
//        异常通知
//        val a = 1/0
    }
}