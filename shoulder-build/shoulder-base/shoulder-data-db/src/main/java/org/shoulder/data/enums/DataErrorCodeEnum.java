package org.shoulder.data.enums;

import org.shoulder.core.exception.ErrorCode;

import javax.annotation.Nonnull;

/**
 * 业务错误码
 *
 * @author lym
 */
public enum DataErrorCodeEnum implements ErrorCode {

    /**
     * 数据版本过旧
     */
    DATA_VERSION_EXPIRED("100", "数据版本过旧，可能已经被其他人修改"),

    /**
     * 数据不存在
     */
    DATA_NOT_EXISTS("110", "数据不存在"),

    /**
     * 数据已存在
     */
    DATA_ALREADY_EXISTS("120", "数据已存在"),

    /**
     * 比如 bizId 通过 md5(name) 生成，更新时发现 bizId 正确，而 name 不正确
     */
    ILLEGAL("122", "数据非法"),
    ;


    /* -------------------------- END -------------------------- */

    /**
     * 标准结果码
     */
    private final String code;

    private final String msg;

    DataErrorCodeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Nonnull
    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return msg;
    }
}