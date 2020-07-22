package org.shoulder.web.filter.xss;

import org.shoulder.web.filter.AbstractPathFilterProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Xss 配置项
 * @author lym
 */
@ConfigurationProperties(prefix = "shoulder.web.waf.xss")
public class XssProperties extends AbstractPathFilterProperties {

}
