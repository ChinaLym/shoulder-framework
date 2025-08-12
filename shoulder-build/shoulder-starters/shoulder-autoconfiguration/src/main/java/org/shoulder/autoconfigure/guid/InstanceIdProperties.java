package org.shoulder.autoconfigure.guid;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.shoulder.autoconfigure.core.BaseAppProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 实例标识
 * 支持集群时才有效
 *
 * @author lym
 */
@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = InstanceIdProperties.PREFIX)
public class InstanceIdProperties {

    public static final String PREFIX = BaseAppProperties.KEY_PREFIX + "instance";

    /**
     * 生成器类型：FIXED / REDIS
     */
    private InstanceIdProviderType type = InstanceIdProviderType.FIXED;

    /**
     * 固定id
     */
    private Long id = 0L;

    private RedisInstantIdProperties redis = new RedisInstantIdProperties();

    @Data
    public static class RedisInstantIdProperties {
        /**
         * redis keyName
         */
        private String assignKey = "{meta}:instance:assign";
        /**
         * redis machineInfoKeyPrefix
         * 注意：修改时需要确保 同一个Lua 脚本中的多个 key 能够位于同一个 hash 槽中！(Redis集群模式限制,若不同在使用redis集群模式时将报错)
         */
        private String machineInfoKeyPrefix = "{meta}:instance:info";

        /**
         * instantId 最大值 todo 这个数要与 GUID 中 instanceId 占用位数(默认10)相关，两者保持一致
         */
        private int max = (1 << 10) - 1;

        /**
         * redis 心跳周期
         */
        private Duration heartbeatPeriod = Duration.ofMinutes(1);

        /**
         * redis 抢占 instantId 的最小时间间隔，如果超过这个时间没有续期，则认为该实例已经挂掉，需要重新获取，并且该 instantId 可被其他机器抢占
         */
        private Duration expiredPeriod = Duration.ofMinutes(20);

    }

    public enum InstanceIdProviderType {

        /**
         * 固定：从配置中获取
         */
        FIXED,

        /**
         * 从 redis 中获取
         */
        REDIS,

        /**
         * 自定义，不使用框架内置算法
         */
        CUSTOMER,
        ;
    }

}
