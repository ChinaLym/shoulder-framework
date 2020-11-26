package org.shoulder.autoconfigure.redis;

import org.shoulder.cluster.redis.annotation.AppExclusive;
import org.shoulder.core.context.AppInfo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * redis相关配置，提供 string 和 string-object 两种
 * 其中 redis 是否为集群是由 RedisConnectionFactory 决定的，spring boot 已经自动支持
 *
 * 但需要注意，Lettuce 默认不会刷新集群拓补图【生成环境将是灾难的】：
 * Redis集群服务某个主节点宕机，对应的从节点会迅速进行迁移升级为主节点（节点迁移期间Redis服务不可用）Jedis会在从节点晋升后正常工作，但 Lettuce不会。
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
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
    @Bean
    @AppExclusive
    public RedisTemplate<String, Object> serviceExclusiveRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        //new StringRedisSerializer(ApplicationInfo.charset())
        RedisSerializer<String> redisKeySerializer = new KeyStringRedisSerializer(getKeyPrefix());
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
        RedisSerializer<String> redisKeySerializer = new KeyStringRedisSerializer(getKeyPrefix());
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
    static class KeyStringRedisSerializer extends StringRedisSerializer {

        /**
         * key 前缀
         */
        private String keyPrefix;

        KeyStringRedisSerializer(String keyPrefix) {
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
