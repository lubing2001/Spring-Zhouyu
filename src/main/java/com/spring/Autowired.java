package com.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // 表示在.class被装载时将被读取，在程序运行期间，将一直保留。
@Target({ElementType.METHOD, ElementType.FIELD})           // 表示这个注解写在方法、属性上
public @interface Autowired {

}
