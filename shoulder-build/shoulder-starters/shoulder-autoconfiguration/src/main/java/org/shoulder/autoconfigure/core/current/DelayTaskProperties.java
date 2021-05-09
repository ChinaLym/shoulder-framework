package org.shoulder.autoconfigure.core.current;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 延迟任务
 *
 * @author lym
 */
@Data
@ConfigurationProperties(prefix = DelayTaskProperties.APPLICATION_CONFIG_PREFIX)
public class DelayTaskProperties {

    public static final String APPLICATION_CONFIG_PREFIX = "shoulder.delay-task";

    /**
     * 延迟任务工具
     */
    private Boolean enable = Boolean.TRUE;

}
