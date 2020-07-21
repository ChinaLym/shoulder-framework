package org.shoulder.web.filter.xss;

import lombok.Data;
import org.shoulder.web.BaseWafProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "waf")
public class XssProperties extends BaseWafProperties {

}
