package org.shoulder.autoconfigure.redis;

import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.util.AddressUtils;
import org.shoulder.core.util.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import redis.embedded.RedisServer;
import redis.embedded.core.RedisServerBuilder;

@ConditionalOnProperty(name = "shoulder.redis.embedded.enable", havingValue = "true", matchIfMissing = false)
@AutoConfiguration(
        before = {RedisAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class,
                RedisAutoConfiguration.class},
        beforeName = "org.springframework.boot.autoconfigure.data.redis.LettuceConnectionConfiguration"
)
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnClass(value = RedisServer.class)
@EnableConfigurationProperties(ShoulderRedisProperties.class)
public class EmbeddedRedisAutoConfiguration {

    @Bean
    @Nullable
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public RedisServer redisServer(ShoulderRedisProperties shoulderRedisProperties) {
        ShoulderRedisProperties.EmbeddedRedisProperties embeddedRedisProperties = shoulderRedisProperties.getEmbedded();
        try {
            // 默认仅 本机访问，其他 ip 机器无法访问内嵌 redis
            RedisServer redisServer = new RedisServerBuilder()
                    .port(embeddedRedisProperties.getPort())
                    .bind(embeddedRedisProperties.getBind())
                    .setting(embeddedRedisProperties.getConfiguration())
                    .build();
            redisServer.start();

            // java进程退出时，自动关闭 redisServer
            //Runtime.getRuntime().addShutdownHook(new Thread(redisServer::stop));
            boolean noPwdNotLocal = !AddressUtils.LOCAL_HOST_IP4.equals(embeddedRedisProperties.getBind()) && StringUtils.containsNone(embeddedRedisProperties.getConfiguration(), "requirepass ");
            ShoulderLoggers.SHOULDER_CONFIG.info("EmbeddedRedis active on {}:{}.", embeddedRedisProperties.getBind(), embeddedRedisProperties.getPort());
            if (noPwdNotLocal) {
                ShoulderLoggers.SHOULDER_CONFIG.warn("Recommend set a password for the redisServer!");
            }
            return redisServer;
        } catch (Exception e) {
            // 用 mock 肯定时本地/单侧，启动失败打印异常
            ShoulderLoggers.SHOULDER_CONFIG
                    .error(CommonErrorCodeEnum.UNKNOWN, e);
            // 考虑到可能本机已经启动一个 redis 导致启动失败，故这里失败不阻塞应用启动
            return null;
        }
    }
}
