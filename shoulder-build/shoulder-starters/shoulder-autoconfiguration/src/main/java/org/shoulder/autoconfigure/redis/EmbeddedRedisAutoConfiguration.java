package org.shoulder.autoconfigure.redis;

import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.ShoulderLoggers;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import redis.embedded.RedisServer;
import redis.embedded.core.RedisServerBuilder;

@ConditionalOnProperty(name = "shoulder.test.mock.redis.enable", havingValue = "true", matchIfMissing = true)
@AutoConfiguration(
        before = {RedisAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class,
                RedisAutoConfiguration.class},
        beforeName = "org.springframework.boot.autoconfigure.data.redis.LettuceConnectionConfiguration"
)
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnClass(value = {RedisTemplate.class, RedisServer.class})
public class EmbeddedRedisAutoConfiguration {

    @Bean
    @Nullable
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public RedisServer redisServer() {
        try {
            // 默认仅 本机访问，其他 ip 机器无法访问内嵌 redis
            RedisServer redisServer = new RedisServerBuilder()
                    .port(6379)
                    //maxHeap
                    .setting("maxmemory 64M")
                    .build();
            redisServer.start();
            // java进程退出时，自动关闭 redisServer
            //Runtime.getRuntime().addShutdownHook(new Thread(redisServer::stop));
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
