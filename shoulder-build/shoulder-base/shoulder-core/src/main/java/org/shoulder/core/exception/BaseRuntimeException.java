package org.shoulder.core.exception;

import jakarta.annotation.Nonnull;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.i18.Translator;
import org.shoulder.core.util.ArrayUtils;
import org.shoulder.core.util.ContextUtils;
import org.shoulder.core.util.ExceptionUtil;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

import java.io.Serial;

/**
 * 基础通用异常
 * 代码中可直接抛出该类异常，会通过统一异常处理将该错误信息捕获封装返回
 * <p>
 * 获取并填充异常信息 {@link ExceptionUtil#generateExceptionMessage}
 * 当A服务被B服务完全替代后，错误码由于前缀必定不兼容，推荐调用者着重看错误码后几位，或由网关隔离，对系统外部调用者做兼容处理
 *
 * @author lym
 */
public class BaseRuntimeException extends RuntimeException implements ErrorCode {

    @Serial private static final long serialVersionUID = 8917762936709850638L;

    /**
     * 错误码，默认unknown
     */
    private String code = CommonErrorCodeEnum.UNKNOWN.getCode();

    /**
     * 异常后，记录日志的级别是什么
     */
    private Level logLevel = Level.ERROR;

    /**
     * 返回给调用方的 HTTP status 是什么
     */
    private HttpStatus httpStatus = DEFAULT_HTTP_STATUS_CODE;

    /**
     * 参数，用于填充 message。message 支持{}、%s这种
     */
    private Object[] args;

    // ==================== 不带错误码的，不推荐使用 =================

    /**
     * 不带错误码的运行异常（传统方式、不推荐）
     *
     * @param cause 上级异常
     */
    public BaseRuntimeException(Throwable cause) {
        super(cause.getMessage(), cause);
        if (cause instanceof ErrorCode errorCode) {
            this.setHttpStatus(errorCode.getHttpStatusCode());
            this.setLogLevel(errorCode.getLogLevel());
            this.setCode(errorCode.getCode());
            this.setArgs(errorCode.getArgs());
        }
    }

    /**
     * 不带错误码的运行异常（传统方式、不推荐）
     *
     * @param message 异常信息
     */
    public BaseRuntimeException(String message) {
        super(message);
    }

    /**
     * 不带错误码的运行异常（传统方式、不推荐）
     *
     * @param message 异常信息
     * @param cause   上级异常
     */
    public BaseRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    // ==================== 推荐使用错误码封装的 =================

    /**
     * 根据定义的错误码直接抛出运行异常（推荐）
     *
     * @param error 规范的错误码，而非堆栈
     */
    public BaseRuntimeException(ErrorCode error) {
        this(error, error.getArgs());
    }

    /**
     * 根据定义的错误码直接抛出运行异常（推荐）
     *
     * @param error 规范的错误码
     * @param args  错误码信息填充参数
     */
    public BaseRuntimeException(ErrorCode error, Object... args) {
        this(error.getCode(), error.getMessage(), error instanceof Throwable ? (Throwable) error : null, args);
    }

    /**
     * 根据定义的错误码直接抛出运行异常（推荐）
     *
     * @param error 规范的错误码
     * @param t     上级异常
     * @param args  错误码信息填充参数
     */
    public BaseRuntimeException(ErrorCode error, Throwable t, Object... args) {
        this(error.getCode(), error.getMessage(), t, args);
        setLogLevel(error.getLogLevel());
        setHttpStatus(error.getHttpStatusCode());
    }

    /**
     * 根据定义的错误码直接抛出运行异常（推荐）
     *
     * @param msg 自定义信息
     */
    public BaseRuntimeException(ErrorCode error, String msg) {
        this(error.getCode(), msg);
    }


    // ==================== 带错误码的 =================

    /**
     * 带错误码的异常（推荐）
     *
     * @param code    错误码
     * @param message 错误描述
     */
    public BaseRuntimeException(String code, String message) {
        this(code, message, new Object[0]);
    }

    /**
     * 带错误码的异常（推荐）
     *
     * @param code    错误码
     * @param message 错误描述
     * @param cause   上級异常
     * @param args    错误信息填充参数
     */
    public BaseRuntimeException(String code, String message, Throwable cause, Object... args) {
        super(ExceptionUtil.generateExceptionMessage(message, args), cause);
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
    public BaseRuntimeException(String code, String message, Object... args) {
        super(ExceptionUtil.generateExceptionMessage(message, args));
        this.code = code;
        this.setArgs(args);
    }

    // ==================== getter/setter =================

    @Nonnull
    @Override
    public String getCode() {
        return code;
    }

    protected void setCode(String code) {
        this.code = code;
    }

    @Override
    public Object[] getArgs() {
        return args == null ? null : args.clone();
    }

    public void setArgs(Object... args) {
        this.args = args == null ? null : args.clone();
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    @Override
    public Level getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    // ----------------------------------------------------------


    /**
     * 转化为 api 返回值
     *
     * @param data 填充异常信息的参数
     * @return api 返回值
     */
    public <T> BaseResult<T> toResponse(T data) {
        return new BaseResult<>(this.getCode(), this.getMessage(), data);
    }

    /**
     * 转化为 api 返回值
     *
     * @return api 返回值
     */
    public BaseResult<Object> toResponse() {
        return new BaseResult<>(this.getCode(), this.getMessage(), ArrayUtils.isEmpty(getArgs()) ? null : getArgs());
    }

    @Override
    public String getLocalizedMessage() {
        return ContextUtils.getBeanOptional(Translator.class)
                .map(t -> t.getMessage(this))
                .orElse(getMessage());
    }


}
