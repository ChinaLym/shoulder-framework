package org.shoulder.autoconfigure.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;


/**
 * 集群模式时激活
 *
 * @author lym
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(ClusterCondition.class)
public @interface ConditionalOnCluster {

    /**
     * true 集群模式激活
     * false 非集群模式激活
     */
    boolean cluster() default true;
}
