package org.shoulder.core.exception;

import org.shoulder.core.i18.Translatable;
import org.shoulder.core.util.ExceptionUtil;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import javax.annotation.Nonnull;


/**
 * 表示错误的接口
 * 必带错误码，可带提示信息的异常/错误
 * 通常错误码枚举、自定义异常类实现该接口
 * <p>
 * 小提示：业务中可以按模块或按业务定义枚举 保存错误码、错误提示信息
 * 详细的错误码规范见 <a href="https://spec.itlym.cn/specs/base/errorCode.html">错误码规范<a/>
 *
 * @author lym
 */
public interface ErrorCode extends Translatable {

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
     * 获取完整错误码
     *
     * @return 错误码
     */
    @Nonnull
    String getCode();

    /**
     * 获取错误信息【非该错误码对应的翻译，而是错误提示，用于日志输出】
     *
     * @return 错误信息，支持 %s、{} 这种带占位符的消息
     */
    String getMessage();

    // --------------------------- 【用于支持多语言】 -----------------------------

    /**
     * 错误码对应的翻译
     *
     * @return 错误码对应的翻译
     */
    @Override
    default String getI18nKey() {
        return "err." + getCode() + ".desc";
    }

    /**
     * 补充翻译需要参数
     * @return 翻译需要参数
     */
    @Override
    @Nullable
    default Object[] getArguments() {
        return getArgs();
    }

    /**
     * 当翻译失败时，返回什么
     * @return msg
     */
    @Override
    @Nullable
    default String getDefaultMessage() {
        return getMessage();
    }


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
    /*default void setArgs(Object... args) {
        throw new UnsupportedOperationException("not support set args");
    }
*/

    /**
     * 发生该错误时用什么级别记录日志【便于全局异常统一处理，非必需】
     * 用户 / 开放接口易触发的错误定为 INFO，如填写参数错误
     * 故意 / 无意未遵守交互约定、外部依赖偶现失败、的记录 WARN
     * 核心业务失败，ERROR
     * 强依赖的服务连接失败 FATAL
     * 详细的日志级别建议见 <a href="https://spec.itlym.cn/specs/base/log.html">软件开发日志规范<a/> - 日志级别规范
     *
     * @return 日志级别
     */
    @Nonnull
    default Level getLogLevel() {
        return DEFAULT_LOG_LEVEL;
    }

    /**
     * 若接口中抛出该错误，返回调用方什么状态码，默认 500 【便于全局异常统一处理，非必需】
     *
     * @deprecated shoulder 默认全都使用 200
     * @return httpStatusCode
     */
    @Nonnull
    default HttpStatus getHttpStatusCode() {
        return DEFAULT_HTTP_STATUS_CODE;
    }


    // --------------------------- 【语法糖方法】 -----------------------------

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
     * 快速转为异常
     *
     * @param args 用于填充翻译项: 可以是普通字符串，也可以为 {@link Translatable}
     * @return 异常
     */
    default BaseRuntimeException toException(Object... args) {
        return new BaseRuntimeException(this, args);
    }

    /**
     * 快速转为异常
     *
     * @param t    上级异常
     * @param args 用于填充翻译项: 可以是普通字符串，也可以为 {@link Translatable}
     * @return 异常
     */
    default BaseRuntimeException toException(Throwable t, Object... args) {
        return new BaseRuntimeException(this, t, args);
    }


    /**
     * --------------------------- 【标识处理成功的返回值】 -----------------------------
     */
    class SuccessCode implements ErrorCode {

        @Nonnull
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


        /**
         * 快速转为异常
         *
         * @param args 用于填充翻译项: 可以是普通字符串，也可以为 {@link Translatable}
         * @return 异常
         */
        @Override
        public BaseRuntimeException toException(Object... args) {
            throw new IllegalCallerException("SuccessCode can't convert to an exception!");
        }

        /**
         * 快速转为异常
         *
         * @param t    上级异常
         * @param args 用于填充翻译项: 可以是普通字符串，也可以为 {@link Translatable}
         * @return 异常
         */
        @Override
        public BaseRuntimeException toException(Throwable t, Object... args) {
            throw new IllegalCallerException("SuccessCode can't convert to an exception!");
        }

    }


}
