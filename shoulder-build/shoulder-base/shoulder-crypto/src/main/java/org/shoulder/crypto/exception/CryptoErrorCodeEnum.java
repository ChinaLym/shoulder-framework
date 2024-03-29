package org.shoulder.crypto.exception;

import jakarta.annotation.Nonnull;
import org.shoulder.core.exception.ErrorCode;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;


/**
 * 加解密错误码
 * 5000-6000
 * 这里定义的异常必须是可以对外部抛出的，不应把全部加解密异常定义错误码或转为运行时异常，该部分应当尽量减少可对外暴露的细节
 *
 * @author lym
 */
public enum CryptoErrorCodeEnum implements ErrorCode {

    /**
     * 加密失败
     */
    ENCRYPT_FAIL(5000, "encrypt fail."),
    /**
     * 解密失败
     */
    DECRYPT_FAIL(5001, "decrypt fail."),

    /**
     * 密钥过期
     */
    KEY_EXPIRED(0, "key expired."),

    /**
     * 密钥无效
     */
    KEY_INVALID(0, "key invalid."),

    /**
     * 证书过期
     */
    CERTIFICATE_EXPIRED(0, "certificate expired."),

    /**
     * 证书无效
     */
    CERTIFICATE_INVALID(0, "certificate invalid."),

    /**
     * 签名失败
     */
    SIGN_FAIL(0, "sign fail"),
    /**
     * 签名验证失败
     */
    SIGN_VERIFY_FAIL(0, "sign verify fail"),
    /**
     * 签名验证失败
     */
    NO_SUCH_KEY_PAIR(0, "no such key pair"),

    ;

    private String code;

    private String message;

    private Level logLevel;

    private HttpStatus httpStatus;

    CryptoErrorCodeEnum(String code) {
        this.code = code;
    }

    CryptoErrorCodeEnum(long code, String message) {
        this(code, message, Level.ERROR);
    }

    CryptoErrorCodeEnum(long code, String message, Level logLevel) {
        this(code, message, logLevel, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    CryptoErrorCodeEnum(long code, String message, Level logLevel, HttpStatus httpStatus) {
        String hex = Long.toHexString(code);
        this.code = "0x" + "0".repeat(Math.max(0, 8 - hex.length())) + hex;
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

    // 改为 CipherRuntimeException

    @Override
    public CipherRuntimeException toException(Object... args) {
        return new CipherRuntimeException(this, args);
    }

    @Override
    public CipherRuntimeException toException(Throwable t, Object... args) {
        return new CipherRuntimeException(this, t, args);
    }

}
