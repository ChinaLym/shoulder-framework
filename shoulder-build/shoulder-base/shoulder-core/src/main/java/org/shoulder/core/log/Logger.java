package org.shoulder.core.log;

import org.shoulder.core.exception.ErrorCode;

/**
 * 在 slf4j 的基础上添加带错误码的记录方式
 *
 * @author lym
 */
public interface Logger extends org.slf4j.Logger {

    // 定义带错误码的日志打印方法

    /**
     * 自动识别日志级别的记录方式，语法糖
     *
     * @param error 错误码
     */
    void log(ErrorCode error);

    // info/debug 级别日志太多，一般不采集，这类日志带错误码对分析意义较小，但又消耗额外的资源（存储/计算/人力编码），故可不带错误码

    void info(ErrorCode errorCode);

    void debug(ErrorCode errorCode);

    /**
     * 推荐的 warn 日志，带错误码
     *
     * @param errorCode 错误码类 / 枚举 / 异常
     */
    void warn(ErrorCode errorCode);

    /**
     * warn 级别带错误码
     *
     * @param errorCode 错误码类 / 枚举
     * @param t         上级异常
     */
    void warn(ErrorCode errorCode, Throwable t);

    void warnWithErrorCode(String errorCode, String msg);


    void warnWithErrorCode(String errorCode, String format, Object arg);


    void warnWithErrorCode(String errorCode, String format, Object... arguments);


    void warnWithErrorCode(String errorCode, String format, Object arg1, Object arg2);


    void warnWithErrorCode(String errorCode, String msg, Throwable t);


    /**
     * error 级别带错误码
     *
     * @param errorCode 错误码类 / 枚举 / 异常
     */
    void error(ErrorCode errorCode);

    /**
     * error 级别带错误码
     *
     * @param errorCode 错误码类 / 枚举
     * @param t         上级异常
     */
    void error(ErrorCode errorCode, Throwable t);

    void errorWithErrorCode(String errorCode, String msg);


    void errorWithErrorCode(String errorCode, String format, Object arg);


    void errorWithErrorCode(String errorCode, String format, Object arg1, Object arg2);


    void errorWithErrorCode(String errorCode, String format, Object... arguments);


    void errorWithErrorCode(String errorCode, String msg, Throwable t);

}
