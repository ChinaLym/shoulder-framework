package org.shoulder.core.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.shoulder.core.context.AppInfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;

/**
 * 错误信息处理类。
 *
 * @author lym
 */
public class ExceptionUtil {

    /**
     * 获取 exception 堆栈的详细错误信息。
     *
     * @param e 抛出的异常
     */
    public static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        return sw.toString();
    }

    /**
     * 格式化异常 msg 信息，一般用于记录日志、api接口出错时使用，返回给前端不需要格式化，而是交给前端格式化
     * 若频繁使用，为了更好的性能：可缓存 MessageFormat。注意：这里是格式化，填充参数，并不是多语言。
     *
     * @param message 错误信息
     * @param args    参数
     * @return 填充参数后的信息
     */
    public static String generateExceptionMessage(String message, Object... args) {
        if (StringUtils.isNotEmpty(message)) {
            message = MessageFormat.format(message, args);
        }
        return message;
    }

    public static String generateExceptionMessageWithCode(String errorCode, String message, Object... args) {
        return "[errorCode=" + errorCode + "] " + generateExceptionMessage(message, args);
    }

    /**
     * 获取根异常
     *
     * @param e 异常信息
     * @return 根异常的错误描述
     */
    public static String getRootErrorMessage(Exception e) {
        Throwable root = ExceptionUtils.getRootCause(e);
        root = (root == null ? e : root);
        if (root == null) {
            return "";
        }
        String msg = root.getMessage();
        if (msg == null) {
            return "null";
        }
        return StringUtils.defaultString(msg);
    }


    public static String formatErrorCode(String errorCode) {
        if ("0".equals(errorCode)) {
            return "0";
        }
        return AppInfo.errorCodePrefix() + errorCode;
    }

    public static String formatErrorCode(Long errorCode) {
        return formatErrorCode(String.format("%08x", errorCode));
    }

}
