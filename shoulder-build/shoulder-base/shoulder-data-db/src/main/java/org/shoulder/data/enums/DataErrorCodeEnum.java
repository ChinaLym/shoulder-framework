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
    DATA_VERSION_EXPIRED(100, "数据版本过旧，可能已经被其他人修改"),

    /**
     * 数据不存在
     */
    DATA_NOT_EXISTS(110, "数据不存在"),

    /**
     * 数据已存在
     */
    DATA_ALREADY_EXISTS(120, "数据已存在"),

    /**
     * 比如 bizId 通过 md5(name) 生成，更新时发现 bizId 正确，而 name 不正确
     */
    ILLEGAL(122, "数据非法"),

    /**
     * 请求数据过多
     */
    DATA_TOO_MUCH(123, "请求数据过多"),
    ;


    /* -------------------------- END -------------------------- */

    /**
     * 标准结果码
     */
    private final String code;

    private final String msg;

    DataErrorCodeEnum(long code, String msg) {
        String hex = Long.toHexString(code);
        this.code = "0x" + "0".repeat(Math.max(0, 8 - hex.length())) + hex;
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
