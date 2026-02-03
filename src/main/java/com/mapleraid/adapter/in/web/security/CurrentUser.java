package com.mapleraid.adapter.in.web.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
    /**
     * 인증이 필수인지 여부. true이면 인증되지 않은 경우 UnauthorizedException 발생
     */
    boolean required() default true;
}
