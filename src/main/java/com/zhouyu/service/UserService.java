package com.zhouyu.service;

import com.spring.*;

@Component("userService")
public class UserService implements UserInterface {

    @Autowired
    private OrderService orderService;

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void test(){
        System.out.println(orderService);
        System.out.println(name);
    }

}
