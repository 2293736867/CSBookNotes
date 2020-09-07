import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello");
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        ((TestInterface)context.getBean("test")).hello();
    }
}
