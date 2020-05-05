package org.shoulder.autoconfigure.property;

import org.shoulder.core.constant.ShoulderFramework;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author lym
 */
@ConfigurationProperties(prefix = ShoulderFramework.CONFIG_PREFIX)
public class ClusterProperties {

    /**
     * 是否支持集群
     */
    private boolean cluster = false;


}
