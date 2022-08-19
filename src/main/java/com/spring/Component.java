package com.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // 表示在.class被装载时将被读取，在程序运行期间，将一直保留。
@Target(ElementType.TYPE)           // 表示这个注解只能写在类上
public @interface Component {

    String value() default "";      // 表示beanName
}
