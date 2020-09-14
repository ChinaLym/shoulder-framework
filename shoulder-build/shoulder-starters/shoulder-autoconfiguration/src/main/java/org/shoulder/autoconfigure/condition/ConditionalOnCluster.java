package org.shoulder.autoconfigure.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;


/**
 * 自动装配条件: 根据是否开启集群模式决定是否激活 Bean
 *
 * @author lym
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(OnClusterCondition.class)
public @interface ConditionalOnCluster {

    /**
     * true 集群模式激活
     * false 非集群模式激活
     */
    boolean cluster() default true;
}
