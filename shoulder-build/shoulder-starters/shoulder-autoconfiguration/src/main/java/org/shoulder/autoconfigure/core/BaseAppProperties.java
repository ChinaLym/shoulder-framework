package org.shoulder.autoconfigure.core;

import lombok.Data;
import org.shoulder.core.constant.ShoulderFramework;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 应用信息，启动后不再变化
 * @author lym
 */
@Data
@Validated
@ConfigurationProperties(prefix = ShoulderFramework.CONFIG_PREFIX + "application")
public class BaseAppProperties {

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
    private String version = "unknown";

    /**
     * 全局统一日期格式
     */
    private String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

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

}
