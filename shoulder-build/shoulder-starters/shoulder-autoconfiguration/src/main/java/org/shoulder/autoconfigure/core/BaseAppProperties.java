package org.shoulder.autoconfigure.core;

import lombok.Data;
import org.shoulder.core.constant.ShoulderFramework;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.i18.LocaleInfo;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 应用信息，启动后不再变化
 *
 * @author lym
 */
@Data
@ConfigurationProperties(prefix = ShoulderFramework.NAME)
public class BaseAppProperties {

    public static final String KEY_PREFIX = ShoulderFramework.NAME + ".";

    public static final String dateTimeFormatConfigPath = "shoulder.application.dateTimeFormat";

    public static final String concurrentEnhanceConfigPath = "shoulder.concurrent.enhance";

    private ShoulderApplicationProperties application = new ShoulderApplicationProperties();

    @Data
    public static class ShoulderApplicationProperties {

        /**
         * 应用标识 appId/identify，推荐为 maven 的 artifactId。若不填写则取 spring.application.name
         */
        private String id;

        /**
         * 应用错误码前缀【推荐填写】
         */
        private String errorCodePrefix = AppInfo.errorCodePrefix();

        /**
         * 应用版本（用于灰度发布、版本兼容等）【推荐填写】 的 version
         */
        private String version = "v1";

        /**
         * 全局统一日期格式，默认为UTC时间格式: yyyy-MM-dd'T'HH:mm:ss.SSS Z
         */
        private String dateTimeFormat = AppInfo.UTC_DATE_TIME_FORMAT;

        /**
         * 全局统一字符集，默认为 UTF-8
         */
        private String charset = LocaleInfo.getSystemDefault().getCharset().name();

        /**
         * 支持集群、多实例部署
         * 开启后缓存将使用外部缓存，如 redis
         */
        private Boolean cluster = false;

        /**
         * 默认语言与地域，默认中国大陆（简体中文）
         */
        private String defaultLocale = "zh_CN";

        /**
         * 时区，默认 GMT+8:00（北京/上海/台北时间）
         */
        private String timeZone = "GMT+8:00";
    }

    /**
     * 缓存 key 分隔符，默认冒号
     */
    private String cacheKeySplit = ":";

    private ShoulderConcurrentProperties concurrent = new ShoulderConcurrentProperties();

    @Data
    public static class ShoulderConcurrentProperties {

        /**
         * 开启全局线程增强（实验功能，默认开启）
         */
        private Boolean enhance = true;
    }

    public static void main(String[] args) {
        System.out.println("");
    }
}
