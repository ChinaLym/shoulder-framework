package org.shoulder.autoconfigure.redis;

import jakarta.annotation.Nullable;
import org.shoulder.cluster.redis.annotation.AppExclusive;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.util.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

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

    /**
     * 必须配置 redis 相关参数
     * 应用专属
     */
    @Bean(name = "redisTemplate")
    @AppExclusive
    public RedisTemplate<String, Object> serviceExclusiveRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        //new StringRedisSerializer(ApplicationInfo.charset())
        RedisSerializer<String> redisKeySerializer = new WithPrefixKeyStringRedisSerializer(getKeyPrefix());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(redisKeySerializer);
        redisTemplate.setHashKeySerializer(redisKeySerializer);
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
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        RedisSerializer<String> redisKeySerializer = new WithPrefixKeyStringRedisSerializer(getKeyPrefix());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(redisKeySerializer);
        redisTemplate.setHashKeySerializer(redisKeySerializer);
        return redisTemplate;
    }

    /**
     * 获取应用标识
     */
    private String getKeyPrefix() {
        return AppInfo.appId() + ":";
    }

    /**
     * redis key 包装
     */
    static class WithPrefixKeyStringRedisSerializer extends StringRedisSerializer {

        /**
         * key 前缀
         */
        private final String keyPrefix;

        WithPrefixKeyStringRedisSerializer(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }

        /**
         * 存放至 redis 前添加 key 前缀
         */
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
        @Override
        public String deserialize(@Nullable byte[] bytes) {
            String str = super.deserialize(bytes);
            // key can't be null
            assert str != null;
            if (!StringUtils.isEmpty(this.keyPrefix) && str.startsWith(this.keyPrefix)) {
                str = str.substring(this.keyPrefix.length() + 1);
            }
            return str;
        }

    }


}
