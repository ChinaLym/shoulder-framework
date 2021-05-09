package org.shoulder.autoconfigure.guid;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.shoulder.autoconfigure.core.BaseAppProperties;
import org.shoulder.core.guid.impl.SnowFlakeGenerator;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 实例标识
 * 支持集群时才有效
 *
 * @author lym
 */
@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = GuidProperties.PREFIX)
public class GuidProperties {

    public static final String PREFIX = BaseAppProperties.APPLICATION_CONFIG_PREFIX + ".guid";

    /**
     * 元时间戳
     */
    private Long timeEpoch = SnowFlakeGenerator.DEFAULT_TIME_EPOCH;

}
