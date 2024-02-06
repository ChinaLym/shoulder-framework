package org.shoulder.web.filter.xss;

import org.shoulder.web.filter.PathFilterProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Xss 配置项
 * todo 自动装配
 *
 * @author lym
 */
@ConfigurationProperties(prefix = "shoulder.web.waf.xss")
public class XssProperties extends PathFilterProperties {

    Boolean enbale = Boolean.TRUE;

}
