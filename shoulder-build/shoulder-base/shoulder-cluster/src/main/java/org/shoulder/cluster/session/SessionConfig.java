package org.shoulder.cluster.session;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * todo 配置
 * spring-session 支持的存储类型：见 StoreType
 * @author lym
 */
@Configuration
@EnableRedisHttpSession
public class SessionConfig {
    @Bean
    public RedisHttpSessionConfiguration redisHttpSessionConfiguration() {
        RedisHttpSessionConfiguration redisHttpSessionConfiguration = new RedisHttpSessionConfiguration();
        // 设置过期时间
        redisHttpSessionConfiguration.setMaxInactiveIntervalInSeconds(3600);
        redisHttpSessionConfiguration.setCookieSerializer(defaultCookieSerializer());
        return redisHttpSessionConfiguration;
    }

    @Bean
    public DefaultCookieSerializer defaultCookieSerializer() {
        DefaultCookieSerializer defaultCookieSerializer = new DefaultCookieSerializer();
        // 设置同域名下不同项目名问题
        defaultCookieSerializer.setCookiePath("/");
        // 下面为修改同根域名不同二级域名访问问题,不用情况下注释掉
        //  defaultCookieSerializer.setDomainName("itlym.cn");
        return defaultCookieSerializer;
    }
}
