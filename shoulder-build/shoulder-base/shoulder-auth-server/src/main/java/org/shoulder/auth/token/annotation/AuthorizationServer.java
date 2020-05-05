package org.shoulder.auth.token.annotation;

import org.shoulder.auth.token.config.AuthorizationServerConfig;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;

import java.lang.annotation.*;

/**
 * 标注为授权服务器
 * 职责：发布认证token，校验 token 是否有效
 *
 * @author lym
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableAuthorizationServer
@Import(AuthorizationServerConfig.class)
public @interface AuthorizationServer {
}
