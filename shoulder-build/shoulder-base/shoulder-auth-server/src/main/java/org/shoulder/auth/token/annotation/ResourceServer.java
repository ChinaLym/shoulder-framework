package org.shoulder.auth.token.annotation;

import org.shoulder.auth.token.config.ResourceServerConfig;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

import java.lang.annotation.*;

/**
 * 标注为资源服务器
 * 表示该应用中的资源应该受到保护
 *
 * @author lym
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableResourceServer
@Import(ResourceServerConfig.class)
public @interface ResourceServer {
}
