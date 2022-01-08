import com.spring.ZhouyuApplicationContext;
import com.zhouyu.AppConfig;
import com.zhouyu.service.UserInterface;
import com.zhouyu.service.UserService;

import java.lang.reflect.InvocationTargetException;

public class Test {
    public static void main(String[] args) throws Exception {
        //忽略懒加载  扫描----> 创建单例子
        ZhouyuApplicationContext zhouyuApplicationContext=new ZhouyuApplicationContext(AppConfig.class);
        UserInterface userService =(UserInterface) zhouyuApplicationContext.getBean("userService");
        userService.test();
    }
}
