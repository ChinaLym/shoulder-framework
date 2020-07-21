package org.shoulder.web;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "waf")
public class BaseWafProperties {

    /**
     * 拦截请求请求路径。默认拦截所有，支持 antMatcher
     */
    private List<String> pathPatterns = Collections.singletonList("/**");
    /**
     * 排除的路径，支持 antMatcher
     */
    private List<String> excludePathPatterns = Collections.singletonList("/**/health");

    /**
     * 是否生效
     */
    private Boolean enable = true;

}
