package com.iccues.metaanimebackend.annotation;

import com.iccues.metaanimebackend.entity.AuditLog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计日志注解
 * 用于标记需要记录审计日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {

    /**
     * 操作类型
     */
    AuditLog.OperationType operation();

    /**
     * 实体类型
     */
    AuditLog.EntityType entityType();

    /**
     * 操作描述
     */
    String description() default "";
}
