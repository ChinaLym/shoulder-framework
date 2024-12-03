package org.shoulder.autoconfigure.web;

import lombok.Data;
import org.shoulder.autoconfigure.core.BaseAppProperties;
import org.shoulder.web.filter.PathFilterProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author lym
 */
@Data
@ConfigurationProperties(prefix = WebFilterProperties.PREFIX)
public class WebFilterProperties {

    /**
     * EXT 配置 key 前缀：shoulder.{moduleName}.ext.{functionName}.xxx
     */
    public static final String PREFIX = BaseAppProperties.KEY_PREFIX + "web.filter";

    @NestedConfigurationProperty
    private PathFilterProperties xss = new PathFilterProperties();

    @NestedConfigurationProperty
    private PathFilterProperties mockUser = new PathFilterProperties();

}
