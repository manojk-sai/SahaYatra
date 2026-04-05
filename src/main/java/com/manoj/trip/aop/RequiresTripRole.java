package com.manoj.trip.aop;

import com.manoj.trip.enums.MemberRole;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresTripRole {
    MemberRole value();
}
