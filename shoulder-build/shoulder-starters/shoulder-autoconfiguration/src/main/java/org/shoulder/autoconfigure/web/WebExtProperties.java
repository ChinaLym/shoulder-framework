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
@ConfigurationProperties(prefix = WebExtProperties.PREFIX)
public class WebExtProperties {

    /**
     * EXT 配置 key 前缀：shoulder.{moduleName}.ext.{functionName}.xxx
     */
    public static final String PREFIX = BaseAppProperties.KEY_PREFIX + "web.ext";

    /**
     * 字典枚举接口相关配置
     */
    private DictionaryProperties dictionary = new DictionaryProperties();

    /**
     * tag 配置
     */
    private TagProperties tag = new TagProperties();
    private OplogProperties oplog = new OplogProperties();
    private ValidationEndPointProperties validationRule = new ValidationEndPointProperties();

    @Data
    static class DictionaryProperties {

        /**
         * 是否启用
         */
        private Boolean enableEnum = Boolean.TRUE;

        /**
         * 是否启用
         */
        private Boolean enable = Boolean.FALSE;

        /**
         * api 路径
         */
        private String path = "/api/v1/dictionary";

        /**
         * api 路径
         */
        private String pageUrl = "/ui/dictionary/page.html";

        /**
         * 是否忽略 字典类型名 dictionaryType 的大小写，默认否
         */
        private Boolean ignoreDictionaryTypeCase = Boolean.FALSE;

        /**
         * 字典 / 字典项 数据存在哪
         */
        private DictionaryStorageType storageType;

    }

    @Data
    static class TagProperties {

        /**
         * 是否启用
         */
        private Boolean enable = Boolean.FALSE;

        /**
         * api 路径
         */
        private String path = "/api/v1/tags";

    }
    @Data
    static class ValidationEndPointProperties {

        /**
         * 是否启用
         */
        private Boolean enable = Boolean.TRUE;

        /**
         * api 路径
         */
        private String path = "/api/v1/validate/rule";

    }

    @Data
    static class OplogProperties {

        /**
         * 是否启用
         */
        private Boolean enable = Boolean.FALSE;

        /**
         * api 路径
         */
        private String path = "/api/v1/oplogs";

    }

    public enum DictionaryStorageType {

        /**
         * 枚举
         */
        ENUM,
        /**
         * 数据库
         */
        DB,
        ;
    }

}
