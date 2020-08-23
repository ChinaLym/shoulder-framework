package org.shoulder.autoconfigure.core;

import lombok.Data;
import org.shoulder.core.constant.ShoulderFramework;
import org.shoulder.core.context.ApplicationInfo;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 应用信息，启动后不再变化
 *
 * @author lym
 */
@Data
@ConfigurationProperties(prefix = BaseAppProperties.APPLICATION_CONFIG_PREFIX)
public class BaseAppProperties {

    public static final String APPLICATION_CONFIG_PREFIX = ShoulderFramework.CONFIG_PREFIX + "application";

    /**
     * 应用标识 appId/identify，推荐为 maven 的 artifactId。若不填写则取 spring.application.name
     */
    private String id;

    /**
     * 应用错误码前缀【推荐必填】
     */
    private String errorCodePrefix = "";

    /**
     * 应用版本（用于灰度发布、版本兼容等）【推荐填写】 的 version
     */
    private String version = "v1";

    /**
     * 全局统一日期格式，默认为UTC时间格式: yyyy-MM-dd'T'HH:mm:ss.SSS Z
     */
    private String dateFormat = ApplicationInfo.UTC_DATE_FORMAT;

    /**
     * 全局统一字符集，默认为 UTF-8
     */
    private String charset = "UTF-8";

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
