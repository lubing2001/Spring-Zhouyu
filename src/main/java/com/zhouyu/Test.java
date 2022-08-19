package com.zhouyu;

import com.spring.ZhouyuApplicationContext;
import com.zhouyu.service.UserInterface;
import com.zhouyu.service.UserService;

public class Test {

    public static void main(String[] args) {
        ZhouyuApplicationContext applicationContext = new ZhouyuApplicationContext(AppConfig.class);
        UserInterface userService = (UserInterface) applicationContext.getBean("userService");
        userService.test();         // 1. 先执行代理逻辑  2. 再执行业务逻辑
    }
}
