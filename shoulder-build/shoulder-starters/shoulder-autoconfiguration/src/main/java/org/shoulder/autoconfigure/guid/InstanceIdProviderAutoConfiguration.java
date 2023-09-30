package org.shoulder.autoconfigure.guid;

import org.shoulder.autoconfigure.condition.ConditionalOnCluster;
import org.shoulder.autoconfigure.redis.RedisAutoConfiguration;
import org.shoulder.cluster.guid.RedisInstanceIdProvider;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.guid.FixedInstanceIdProvider;
import org.shoulder.core.guid.InstanceIdProvider;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 实例标识分配
 *
 * @author lym
 */
@AutoConfiguration
@ConditionalOnClass(InstanceIdProvider.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(InstanceIdProperties.class)
public class InstanceIdProviderAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(InstanceIdProviderAutoConfiguration.class);

    /**
     * 配置
     */
    private final InstanceIdProperties instanceIdProperties;

    public InstanceIdProviderAutoConfiguration(InstanceIdProperties instanceIdProperties) {
        this.instanceIdProperties = instanceIdProperties;
    }


    /**
     * 默认情况，固定为配置的 id
     *
     * @return fixed
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = InstanceIdProperties.PREFIX + ".type", havingValue = "fixed", matchIfMissing = true)
    public InstanceIdProvider fixedInstanceIdProvider() {
        if(AppInfo.cluster() && instanceIdProperties.getId().equals(0)) {
            log.warn("Active cluster mode, but instanceId is DEFAULT VALUE: 0! Please change shoulder.instance.id in application.properties!");
        }
        return new FixedInstanceIdProvider(instanceIdProperties.getId());
    }


    /**
     * 集群时，引入 redis 情况
     */
    @ConditionalOnCluster(cluster = true)
    @ConditionalOnClass({RedisTemplate.class, org.shoulder.cluster.guid.RedisInstanceIdProvider.class})
    @ConditionalOnProperty(value = InstanceIdProperties.PREFIX + ".type", havingValue = "redis", matchIfMissing = true)
    public static class RedisInstanceIdProviderConfiguration {

        /**
         * 依赖 redis 生成
         *
         * @return redis
         */
        @Bean
        @ConditionalOnMissingBean
        public InstanceIdProvider instanceIdProvider(RedisTemplate redisTemplate) {
            return new RedisInstanceIdProvider(
                    AppInfo.appId() + ":idAssigner",
                    // 这个数要与 GUID 中 instanceId 占用位数(默认10)相关，两者保持一致
                    (1 << 10) - 1,
                    redisTemplate
            );
        }
    }

}
