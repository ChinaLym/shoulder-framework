package org.shoulder.ext.config.domain.enums;

import org.shoulder.core.exception.ErrorCode;

/**
 * 业务错误码
 *
 * @author lym
 */
public enum ConfigErrorCodeEnum implements ErrorCode {

    /**
     * 数据版本过旧
     */
    DATA_VERSION_EXPIRED("100", "数据版本过旧，可能已经被其他人修改"),

    /**
     * 配置类型不存在
     */
    CONFIG_TYPE_NOT_EXISTS("110", "配置类型不存在"),


    CONFIG_DATA_ALREADY_EXISTS("120", "配置项已存在"),

    CONFIG_DATA_NOT_EXISTS("121", "配置项不存在"),

    CONFIG_DATA_MISS_BIZ_ID_FIELDS("123", "配置项业务标识字段为空"),

    CONFIG_DATA_BIZ_ID_CHANGED("122", "配置项业务标识字段被篡改"),

    /* -------------------------- CODING -------------------------- */


    /**
     * 代码写错了 / 语法用错了
     */
    CODING_ERROR("200", "编码错误: the [%s] %s"),
    ;

    /* -------------------------- END -------------------------- */

    /**
     * 标准结果码
     */
    private final String code;

    private final String msg;

    ConfigErrorCodeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return msg;
    }
}