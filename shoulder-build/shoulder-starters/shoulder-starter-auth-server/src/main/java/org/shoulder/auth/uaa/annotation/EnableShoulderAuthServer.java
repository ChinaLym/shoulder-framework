package org.shoulder.auth.uaa.annotation;

import org.shoulder.auth.uaa.configuration.ShoulderAuthServerMarkConfiguration;
import org.springframework.context.annotation.Import;

/**
 * 标记一个应用为认证服务器
 *
 * @author lym
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ShoulderAuthServerMarkConfiguration.class)
public @interface EnableShoulderAuthServer {

}
