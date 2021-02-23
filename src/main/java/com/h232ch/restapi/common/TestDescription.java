package com.h232ch.restapi.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE) // 얼마나 오래 지속할것인지? 기본값은 Class (컴파일 이후? 컴파일 전)
public @interface TestDescription {
    String value();
}
