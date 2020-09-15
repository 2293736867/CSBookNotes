package aspectj_annotation

import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

fun test()
{
    val context = ClassPathXmlApplicationContext("applicationContext.xml") as ApplicationContext
    val annotationInterface = context.getBean("annotationImpl") as AnnotationInterface
    annotationInterface.delete()
    annotationInterface.modify()
    annotationInterface.save()
}
