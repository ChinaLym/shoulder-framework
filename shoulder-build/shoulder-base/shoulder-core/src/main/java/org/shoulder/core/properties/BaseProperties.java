package org.shoulder.core.properties;

import lombok.Data;
import org.shoulder.core.constant.ShoulderFramework;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 *
 * @author lym
 */
@Data
@Validated
@ConfigurationProperties(prefix = ShoulderFramework.NAME)
public class BaseProperties {

    /**
     * 应用标识
     */
    private String appId;

    /**
     * 应用版本
     */
    private String version = "unknown";

    /**
     * 应用错误码前缀
     */
    private String errorCodePrefix;

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

}
