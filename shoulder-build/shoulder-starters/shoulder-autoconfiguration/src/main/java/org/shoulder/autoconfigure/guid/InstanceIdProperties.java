package org.shoulder.autoconfigure.guid;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.shoulder.autoconfigure.core.BaseAppProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

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


    public enum InstanceIdProviderType {

        /**
         * 固定：从配置中获取
         */
        FIXED,

        /**
         * 从 redis 中获取
         */
        REDIS,

        ;
    }

}
