package org.shoulder.crypto.negotiation.exception;

import org.shoulder.core.exception.ErrorCode;
import org.shoulder.crypto.exception.CipherRuntimeException;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

import javax.annotation.Nonnull;


/**
 * 密钥协商错误码
 * 5500-5600
 * 这里定义的异常必须是可以对外部抛出的，不应把全部加解密异常定义错误码或转为运行时异常，该部分应当尽量减少可对外暴露的细节
 *
 * @author lym
 */
public enum NegotiationErrorCodeEnum implements ErrorCode {

    // ------------------------------- 对称加解密 -----------------------------

    /**
     * 参数错误:参数Secusid 为空未传...
     */
    MISSING_REQUIRED_PARAM(5501, "negotiation param missing", Level.WARN, HttpStatus.BAD_REQUEST),

    /**
     * 协商失败
     */
    NEGATION_FAIL(5510, "negotiation fail.", Level.ERROR, HttpStatus.INTERNAL_SERVER_ERROR),

    /**
     * Token无效
     */
    TOKEN_INVALID(5511, "token invalid.", Level.INFO, HttpStatus.FORBIDDEN),

    /**
     * 无效的密钥：未协商，或【已过期】需要重新协商
     */
    NEGOTIATION_INVALID(5512, "negotiation invalid.", Level.INFO, HttpStatus.FORBIDDEN),

    /**
     * 数据加密失败【按理不会出现】：签名失败
     */
    ENCRYPT_FAIL(5513, "negotiation encrypt fail.", Level.ERROR, HttpStatus.INTERNAL_SERVER_ERROR),

    /**
     * 数据解密失败【按理不会出现】：验签失败
     */
    DATA_DECRYPT_FAIL(5514, "negotiation decrypt fail.", Level.WARN, HttpStatus.FORBIDDEN),

    ;

    private String code;

    private String message;

    private Level logLevel;

    private HttpStatus httpStatus;

    NegotiationErrorCodeEnum(long code, String message, Level logLevel) {
        // 默认 403 拒绝访问
        this(code, message, logLevel, HttpStatus.FORBIDDEN);
    }

    NegotiationErrorCodeEnum(long code, String message, Level logLevel, HttpStatus httpStatus) {
        this.code = Long.toHexString(code);
        this.message = message;
        this.logLevel = logLevel;
        this.httpStatus = httpStatus;
    }

    @Nonnull
    @Override
    public String getCode() {
        return code;
    }

    @Nonnull
    @Override
    public String getMessage() {
        return message;
    }

    @Nonnull
    @Override
    public Level getLogLevel() {
        return logLevel;
    }

    @Nonnull
    @Override
    public HttpStatus getHttpStatusCode() {
        return httpStatus;
    }

    // 改为 NegotiationException

    @Override
    public CipherRuntimeException toException(Object... args) {
        return new CipherRuntimeException(this, args);
    }

    @Override
    public CipherRuntimeException toException(Throwable t, Object... args) {
        return new CipherRuntimeException(this, t, args);
    }

}
