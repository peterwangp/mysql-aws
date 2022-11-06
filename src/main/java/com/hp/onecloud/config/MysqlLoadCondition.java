package com.hp.onecloud.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class MysqlLoadCondition implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {

        String mysqlCond = conditionContext.getEnvironment().getProperty("enable_mysql");

        return mysqlCond != null;
    }
}