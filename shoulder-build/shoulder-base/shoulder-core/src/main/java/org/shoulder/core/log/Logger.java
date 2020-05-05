package org.shoulder.core.log;

import org.shoulder.core.exception.IError;

/**
 * 在 slf4j 的基础上添加带错误码的记录方式
 * @author lym
 */
public interface Logger extends org.slf4j.Logger {

    // 定义带错误码的日志打印方法

    /**
     * 推荐的 warn 日志
     * @param error 带错误码和默认提示信息的异常
     */
    void warn(IError error);

    void warnWithErrorCode(String errorCode, String msg);


    void warnWithErrorCode(String errorCode, String format, Object arg);


    void warnWithErrorCode(String errorCode, String format, Object... arguments);


    void warnWithErrorCode(String errorCode, String format, Object arg1, Object arg2);


    void warnWithErrorCode(String errorCode, String msg, Throwable t);


    void error(IError error);

    void errorWithErrorCode(String errorCode, String msg);


    void errorWithErrorCode(String errorCode, String format, Object arg);


    void errorWithErrorCode(String errorCode, String format, Object arg1, Object arg2);


    void errorWithErrorCode(String errorCode, String format, Object... arguments);


    void errorWithErrorCode(String errorCode, String msg, Throwable t);


}
