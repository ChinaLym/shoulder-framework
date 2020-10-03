package org.shoulder.auth.uaa.annotation;

import org.shoulder.auth.uaa.configuration.AuthorizationServerConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 标记一个应用为认证服务器
 * 使用 jwt 认证、
 *
 * @author lym
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AuthorizationServerConfiguration.class)
public @interface EnableShoulderAuthServer {

}
