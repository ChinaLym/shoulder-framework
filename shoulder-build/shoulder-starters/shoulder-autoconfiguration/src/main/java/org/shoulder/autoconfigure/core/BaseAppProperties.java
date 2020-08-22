package org.shoulder.autoconfigure.core;

import lombok.Data;
import org.shoulder.core.constant.ShoulderFramework;
import org.shoulder.core.context.ApplicationInfo;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 应用信息，启动后不再变化
 * @author lym
 */
@Data
@Validated
@ConfigurationProperties(prefix = BaseAppProperties.APPLICATION_CONFIG_PREFIX)
public class BaseAppProperties {

    public static final String APPLICATION_CONFIG_PREFIX = ShoulderFramework.CONFIG_PREFIX + "application";

    /**
     * 应用标识 appId/identify，若不填写，使用 spring.application.name
     */
    private String id;

    /**
     * 应用错误码前缀
     */
    private String errorCodePrefix = "";

    /**
     * 应用版本（用于灰度发布、版本兼容等）
     */
    private String version = "v1";

    /**
     * 全局统一日期格式
     */
    private String dateFormat = ApplicationInfo.UTC_DATE_FORMAT;

    /**
     * 全局统一字符集
     */
    private String charset = "UTF-8";

    /**
     * 支持集群、多实例部署
     * 开启后一些缓存将使用外部缓存，默认使用 redis
     */
    private Boolean cluster = false;

    /**
     * 默认语言与地域
     */
    private String defaultLocale = "zh_CN";

    /**
     * 时区，默认 GMT+8:00（北京/上海/台北时间）
     */
    private String timeZone = "GMT+8:00";

}
