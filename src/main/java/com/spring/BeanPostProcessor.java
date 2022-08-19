package com.spring;


public interface BeanPostProcessor {

    // 初始化前执行这个方法
    Object postProcessBeforeInitialization(Object bean, String beanName);

    // 初始化后执行这个方法
    Object postProcessAfterInitialization(Object bean, String beanName);
}
