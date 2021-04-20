package org.shoulder.security.exception;

import lombok.Getter;
import lombok.Setter;
import org.shoulder.core.exception.ErrorCode;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

/**
 * 在 spring 的 {@link AuthenticationException} 之上添加了错误码
 *
 * @author lym
 * @see ShoulderAuthenticationException
 */
@Getter
@Setter
public class ShoulderAuthenticationException extends AuthenticationException implements ErrorCode {

    /**
     * 错误码
     */
    private String code;

    /**
     * 认证失败时，记录日志采用 WARN 级别
     */
    private Level logLevel = Level.WARN;

    /**
     * 返回给调用方的 HTTP status 是什么
     */
    private HttpStatus httpStatus = DEFAULT_HTTP_STATUS_CODE;

    /**
     * 参数，用于填充 message。message 支持{}、%s这种
     */
    private Object[] args;

    public ShoulderAuthenticationException(String msg) {
        super(msg);
    }

    public ShoulderAuthenticationException(String msg, Throwable e) {
        super(msg, e);
    }

    /**
     * 带错误码的异常（推荐）
     *
     * @param code    错误码
     * @param message 错误描述
     */
    public ShoulderAuthenticationException(String code, String message) {
        this(code, message, (Object) null);
    }

    /**
     * 带错误码的异常（推荐）
     *
     * @param code    错误码
     * @param message 错误描述
     * @param cause   上級异常
     * @param args    错误信息填充参数
     */
    public ShoulderAuthenticationException(String code, String message, Throwable cause, Object... args) {
        super(message, cause);
        this.code = code;
        this.setArgs(args);
    }

    /**
     * 带错误码的异常（推荐）
     *
     * @param code    错误码
     * @param message 错误描述
     * @param args    错误信息填充参数
     */
    public ShoulderAuthenticationException(String code, String message, Object... args) {
        super(message);
        this.code = code;
        this.setArgs(args);
    }

}
