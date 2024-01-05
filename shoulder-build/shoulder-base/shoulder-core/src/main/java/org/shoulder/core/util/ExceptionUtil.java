package org.shoulder.core.util;

import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.exception.ErrorCode;

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
     * 获取 exception 堆栈的详细错误信息
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
        if(ArrayUtils.allNull(args)) {
            // 这里会把 args 丢掉。    暂不考虑 message == null 情况
            return message;
        }
        if (StringUtils.isNotEmpty(message)) {
            // 顺序不要变，否则出问题
            if (message.contains("{}")) {
                // log4j 格式
                Log4jParameterFormatter.formatMessage(message, args);
            } else if (message.contains("{0}")) {
                // MessageFormat
                message = MessageFormat.format(message, args);
            } else if (message.contains("%")) {
                // 否则尝试 String format
                message = String.format(message, args);
            }
            return message;
        }
        // message 不存在这三类占位符，且传了args，直接使用 args
        // message 为空 arg 有值，将 args 连成 string 作为 msg
        return ArrayUtils.concatToStr(args);
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


    public static String formatErrorCode(@Nonnull String errorCode) {
        AssertUtils.notBlank(errorCode, CommonErrorCodeEnum.CODING, "errorCode is null!");
        if (ErrorCode.SUCCESS_CODE.equals(errorCode)) {
            return ErrorCode.SUCCESS_CODE;
        }
        return StringUtils.startsWith(errorCode, "0x") ? errorCode :
                AppInfo.errorCodePrefix() + "0".repeat(Math.max(0, 8 - errorCode.length())) + errorCode;
    }

    public static String formatErrorCode(Long errorCode) {
        AssertUtils.notNull(errorCode, CommonErrorCodeEnum.CODING, "errorCode is null!");
        return formatErrorCode(String.format("%08x", errorCode));
    }

}
