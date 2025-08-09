package org.shoulder.autoconfigure.redis;

import jakarta.annotation.Nullable;
import org.shoulder.cluster.redis.annotation.AppExclusive;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.Nonnull;

/**
 * redis相关配置，提供 string 和 string-object 两种
 * 其中 redis 是否为集群是由 RedisConnectionFactory 决定的，spring boot 已经自动支持【这里推荐在单机并发量低的场景使用同步的jedis，并发高再使用 lettuce/redisson】
 *
 * @author lym
 */
@AutoConfiguration(before = org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class)
@ConditionalOnClass(RedisTemplate.class)
public class RedisAutoConfiguration {

    /*@SuppressWarnings("unchecked")
    @Bean
    @ConditionalOnMissingBean
    public GlobalLock globalLock(StringRedisTemplate redisTemplate){
        RedisSimpleGlobalLock lock = new RedisSimpleGlobalLock(redisTemplate);
        lock.setAppName(AppInfo.appId());
        return lock;
    }*/

    @Bean
    @ConditionalOnMissingBean(StringRedisSerializer.class)
    public WithPrefixKeyStringRedisSerializer withPrefixKeyStringRedisSerializer(@Value("${shoulder.redis.key-prefix:}")String redisKeyPrefix) {
        if(StringUtils.isEmpty(redisKeyPrefix)) {
            redisKeyPrefix = AppInfo.appId() + AppInfo.cacheKeySplit();
        }
        return new WithPrefixKeyStringRedisSerializer(redisKeyPrefix);
    }

    /**
     * 必须配置 redis 相关参数
     * 应用专属
     */
    @Bean(name = "redisTemplate")
    @AppExclusive
    public RedisTemplate<String, Object> serviceExclusiveRedisTemplate(RedisConnectionFactory redisConnectionFactory, StringRedisSerializer stringRedisSerializer) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        //new StringRedisSerializer(ApplicationInfo.charset())
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(RedisSerializer.json());
        redisTemplate.setHashValueSerializer(RedisSerializer.json());
        return redisTemplate;
    }

    /**
     * 必须配置 redis 相关参数
     * 应用专属
     */
    @Bean
    @AppExclusive
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory, StringRedisSerializer stringRedisSerializer) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        return redisTemplate;
    }

    /**
     * redis key 包装
     */
    public static class WithPrefixKeyStringRedisSerializer extends StringRedisSerializer {

        /**
         * key 前缀
         */
        private final String keyPrefix;

        public WithPrefixKeyStringRedisSerializer(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }

        /**
         * 存放至 redis 前添加 key 前缀
         */
        @Nonnull
        @Override
        public byte[] serialize(@Nullable String string) {
            if (!StringUtils.isEmpty(this.keyPrefix)) {
                string = this.keyPrefix + string;
            }
            return super.serialize(string);
        }

        /**
         * 取出时去掉 key 前缀
         */
        @Nonnull
        @Override
        public String deserialize(@Nullable byte[] bytes) {
            String str = super.deserialize(bytes);
            if (!StringUtils.isEmpty(this.keyPrefix) && str.startsWith(this.keyPrefix)) {
                str = str.substring(this.keyPrefix.length());
            }
            return str;
        }

    }


}
