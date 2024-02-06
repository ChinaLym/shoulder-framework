package org.shoulder.data.enums;

import jakarta.annotation.Nonnull;
import org.shoulder.core.exception.ErrorCode;

/**
 * 业务错误码
 *
 * @author lym
 */
public enum DataErrorCodeEnum implements ErrorCode {

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
