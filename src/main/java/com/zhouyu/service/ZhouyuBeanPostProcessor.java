package com.zhouyu.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/**
 * 加上 @Component 注解，在spring中注册
 * 扫描时发现这个类实现了 BeanPostProcessor 接口，Spring 就知道了当前这个bean是一个比较特殊的bean
 * 和UserService不一样，Spring 等会还要来调用这个方法的
 */
@Component
public class ZhouyuBeanPostProcessor implements BeanPostProcessor {

    // spring在创建任何bean的时候都会执行下面的两个方法

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        // 初始化前调用这个方法
        // 针对一个bean或者多个bean同时处理都行
//        if(beanName.equals("userService")){
//            System.out.println("初始化前");
//            ((UserService)bean).setName("周瑜好帅");
//        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        // 初始化后调用这个方法
        System.out.println("初始化后");
        // 匹配
        if(beanName.equals("userService")){

            // 使用JDK的动态代理生成一个代理对象                                                           // 代理的接口是当前这个bean
            Object proxyInstance = Proxy.newProxyInstance(ZhouyuBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("代理逻辑");     // 找切点
                    return method.invoke(bean, args);  // 执行被代理对象原来的方法并返回
                }
            });
            return proxyInstance;
        }

        return bean;
    }
}
