package org.shoulder.autoconfigure.web;

import lombok.Data;
import org.shoulder.autoconfigure.core.BaseAppProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * web 扩展能力配置
 *
 * @author lym
 */
@Data
@ConfigurationProperties(prefix = WebExProperties.PREFIX)
public class WebExProperties {

    public static final String PREFIX = BaseAppProperties.KEY_PREFIX + "web.ext";

    /**
     * 字典枚举接口相关配置
     */
    private DictionaryProperties dictionary;

    @Data
    static class DictionaryProperties {

        /**
         * 是否启用
         */
        private Boolean enable = Boolean.TRUE;

        /**
         * 是否忽略 字典类型名 dictionaryType 的大小写，默认否
         */
        private Boolean ignoreDictionaryTypeCase = Boolean.FALSE;

        /**
         * 字典 api 路径
         */
        private String path = "/api/v1/dictionary";

    }

}
