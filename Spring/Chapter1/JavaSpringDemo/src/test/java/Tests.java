import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Tests {
    @Test
    public void test()
    {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        TestInterface testInterface = (TestInterface)context.getBean("test");
        testInterface.hello();
    }
}
