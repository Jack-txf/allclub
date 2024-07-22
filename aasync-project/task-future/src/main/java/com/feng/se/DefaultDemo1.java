package com.feng.se;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Williams_Tian
 * @CreateDate 2024/7/22
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultDemo1 {
    String getUser() default "defaultUser"; // 这个写法只能在注解里面
}
