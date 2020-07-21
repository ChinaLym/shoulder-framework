package org.shoulder.core.properties;

import lombok.Data;

/**
 *
 * @author lym
 */
@Data
public class CommonProperties {

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

}
