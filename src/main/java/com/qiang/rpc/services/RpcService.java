package com.qiang.rpc.services;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 接口、类、枚举、注解
@Target({ElementType.TYPE})
// 注解会在class字节码文件中存在，在运行时可以通过反射获取到
@Retention(RetentionPolicy.RUNTIME)
// 组件
@Component
public @interface RpcService {
    String name() default "";

    Class<?> value();
}
