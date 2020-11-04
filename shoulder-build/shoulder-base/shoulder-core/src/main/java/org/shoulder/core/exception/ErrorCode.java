package org.shoulder.core.exception;

import org.shoulder.core.dto.response.RestResult;
import org.shoulder.core.i18.Translatable;
import org.shoulder.core.util.ExceptionUtil;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

/**
 * 表示错误的接口
 * 必带错误码，可带提示信息的异常/错误
 * 通常错误码枚举、自定义异常类实现该接口
 * <p>
 * 小提示：业务中可以按模块或按业务定义枚举 保存错误码、错误提示信息
 *
 * @author lym
 */
public interface ErrorCode {

    /**
     * 默认错误码、异常类日志记录级别为 warn
     */
    Level DEFAULT_LOG_LEVEL = Level.WARN;

    /**
     * 默认错误码、异常类 HTTP 响应码为 500
     */
    HttpStatus DEFAULT_HTTP_STATUS_CODE = HttpStatus.INTERNAL_SERVER_ERROR;

    /**
     * 特殊值，0代表成功
     */
    SuccessCode SUCCESS = new SuccessCode();

    /**
     * 获取错误码（不带前缀）
     *
     * @return 错误码
     */
    @NonNull
    String getCode();

    /**
     * 获取错误信息
     *
     * @return 错误信息，支持 %s、{} 这种带占位符的消息
     */
    String getMessage();

    // --------------------------- 【便于全局异常统一处理的方法】 -----------------------------

    /**
     * 获取用于填充错误信息的数据 【便于全局异常统一处理，非必需】
     *
     * @return 用于填充错误信息的数据
     */
    default Object[] getArgs() {
        return new Object[0];
    }

    /**
     * 设置用于填充错误信息的数据 【便于全局异常统一处理，非必需】
     *
     * @param args 用于填充错误信息的数据
     */
    default void setArgs(Object... args) {
        throw new UnsupportedOperationException("not support set args");
    }

    /**
     * 发生该错误时用什么级别记录日志【便于全局异常统一处理，非必需】
     *
     * @return 日志级别
     */
    @NonNull
    default Level getLogLevel() {
        return DEFAULT_LOG_LEVEL;
    }

    /**
     * 若接口中抛出该错误，返回调用方什么状态码，默认 500 【便于全局异常统一处理，非必需】
     *
     * @return httpStatusCode
     */
    @NonNull
    default HttpStatus getHttpStatusCode() {
        return DEFAULT_HTTP_STATUS_CODE;
    }


    // --------------------------- 【语法糖方法】 -----------------------------

    /**
     * 转化为 api 返回值
     *
     * @param args 填充异常信息的参数
     * @return api 返回值
     */
    default RestResult<Object[]> toResponse(Object... args) {
        return new RestResult<>(
            this.getCode(),
            this.getMessage(),
            args == null ? getArgs() : args
        );
    }

    /**
     * 生成详情
     * 仅在作为接口返回值、记录本地日志信息时使用
     *
     * @return 填充参数后的 msg
     */
    default String generateDetail() {
        return ExceptionUtil.generateExceptionMessage(getMessage(), getArgs());
    }

    /**
     * 抛出运行异常
     *
     * @throws BaseRuntimeException e
     */
    default void throwRuntime() throws BaseRuntimeException {
        throw new BaseRuntimeException(this);
    }

    /**
     * 抛出运行异常
     *
     * @param args 用于填充翻译项，可以为普通字符串，也可以为 {@link Translatable}
     * @throws BaseRuntimeException e
     */
    default void throwRuntime(Object... args) throws BaseRuntimeException {
        throw new BaseRuntimeException(this, args);
    }

    /**
     * 抛出运行异常
     *
     * @param t 上级异常（直接异常）
     * @param args 用于填充翻译项，可以为普通字符串，也可以为 {@link Translatable}
     * @throws BaseRuntimeException e
     */
    default void throwRuntime(Throwable t, Object... args) throws BaseRuntimeException {
        throw new BaseRuntimeException(this, args);
    }

    /**
     * 包装 t 并抛出运行异常
     *
     * @param t 异常
     * @throws BaseRuntimeException e
     */
    default void throwRuntime(Throwable t) throws BaseRuntimeException {
        throw new BaseRuntimeException(getCode(), getMessage(), t);
    }

    /**
     * --------------------------- 【标识处理成功的返回值】 -----------------------------
     */
    class SuccessCode implements ErrorCode {

        @NonNull
        @Override
        public String getCode() {
            return "0";
        }

        @Override
        public String getMessage() {
            return "success";
        }

        @Override
        public Level getLogLevel() {
            return Level.DEBUG;
        }

        @Override
        public HttpStatus getHttpStatusCode() {
            return HttpStatus.OK;
        }

        @Override
        public void throwRuntime() throws BaseRuntimeException {
            // doNothing
        }

        @Override
        public void throwRuntime(Throwable t) throws BaseRuntimeException {
            // doNothing
        }
    }


}
