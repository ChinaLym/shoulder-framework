package org.shoulder.autoconfigure.redis;

import org.springframework.context.annotation.Import;
import redis.embedded.RedisServer;

import java.lang.annotation.*;

/**
 * （可选）启用内嵌 Redis：标记在启动类后，会在启动时尽早的激活 EmbeddedRedisServer
 *  <p>
 * {@link EmbeddedRedisAutoConfiguration} 默认会检测 maven 依赖，自动装配
 * 该注解仅用于提早激活 EmbeddedRedisServer的时间
 * 应对使用者的项目中 @Import 或其他特殊情况意外过早的触发了 redis 的使用，EmbeddedRedisServer 加载晚了，从而导致应用因 redis 连接失败而启动失败
 *
 * @author lym
 * @see RedisServer 若只使用注解，而未引入相关 jar/class，则该配置不生效
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EmbeddedRedisAutoConfiguration.class)
public @interface EnableEmbeddedRedis {

    int port() default 6379;

    String startParam() default "maxmemory 64M";

}
