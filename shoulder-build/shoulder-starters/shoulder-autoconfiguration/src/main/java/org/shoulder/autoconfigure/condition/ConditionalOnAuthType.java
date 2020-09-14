package org.shoulder.autoconfigure.condition;

import org.shoulder.security.authentication.AuthenticationType;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;


/**
 * 自动装配条件: 根据认证方式决定是否激活 Bean
 *
 * @author lym
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(OnAuthTypeCondition.class)
public @interface ConditionalOnAuthType {

    /**
     * SESSION session 模式生效
     * TOKEN token 模式生效
     */
    AuthenticationType type() default AuthenticationType.SESSION;
}
