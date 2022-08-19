package com.spring;

public class BeanDefinition {

    private Class clazz;    // 表示当前某一个bean的类型
    private String scope;   // 当前bean的作用域（单例还是多例）


    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
