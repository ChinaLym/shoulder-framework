package org.shoulder.core.log;

import org.shoulder.core.context.AppContext;
import org.shoulder.core.exception.ErrorCode;
import org.shoulder.core.util.ExceptionUtil;
import org.shoulder.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.event.Level;

/**
 * 在 slf4j 之上封装一层带错误码的记录实现
 * 统一使用该 logger 记录日志，禁用 {@link System#out}
 * trace、debug、info 这些可能不输出的日志的级别采用占位符或条件判断，warn、error则推荐字符串拼接避免占位符查找替换，参数非法一般用warn
 * 推荐使用者开启异步日志优化IO（默认关闭）
 * 默认info以及以下级别，不配置日志框架输出日志打印处的类名，方法名及行号的信息（获取堆栈信息较消耗资源，应通过配置项是否开启）
 *
 * @author lym
 */
public class ShoulderLogger implements org.shoulder.core.log.Logger {

    /**
     * 已经格式化的错误码信息
     */
    private static final String MDC_ERROR_CODE_NAME = "S-ERR_CODE";

    /**
     * 已经格式化的调用链信息
     * 使用 spring cloud sleuth 中定义的 B3Propagation
     */
    private static final String MDC_TRACE_NAME = "S-TRACE";

    // MDC key

    private static final String MDC_PARENT_ID = "parent_id";
    private static final String MDC_TRACE_ID = "trace_id";
    private static final String MDC_SPAN_ID = "span_id";

    // 定义前后缀，以便于日志分割和分析

    private static final String ERROR_CODE_PREFIX = "[";
    private static final String ERROR_CODE_SUFFIX = "]";

    private static final String TRACE_PREFIX = "<";
    private static final String DELIMITER = ",";
    private static final String TRACE_SUFFIX = ">";

    /**
     * Slf4j 的 logger
     */
    protected final Logger delegateLogger;

    /**
     * 构造器使用 {@link org.shoulder.core.log.LoggerFactory#getLogger}
     *
     * @param name 日志记录器名称
     */
    ShoulderLogger(String name) {
        delegateLogger = LoggerFactory.getLogger(name);
    }

    @Override
    public String getName() {
        return delegateLogger.getName();
    }

    // -------------------------------------- TRACE 级别日志 -------------------------------------------

    @Override
    public boolean isTraceEnabled() {
        return delegateLogger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        uniformLog(() -> delegateLogger.trace(msg));
    }

    @Override
    public void trace(String format, Object arg) {
        uniformLog(() -> delegateLogger.trace(format, arg));
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        uniformLog(() -> delegateLogger.trace(format, arg1, arg2));
    }

    @Override
    public void trace(String format, Object... arguments) {
        uniformLog(() -> delegateLogger.trace(format, arguments));
    }

    @Override
    public void trace(String msg, Throwable t) {
        uniformLog(() -> delegateLogger.trace(msg, t));
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return delegateLogger.isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String msg) {
        uniformLog(() -> delegateLogger.trace(marker, msg));
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        uniformLog(() -> delegateLogger.trace(marker, format, arg));
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        uniformLog(() -> delegateLogger.trace(marker, format, arg1, arg2));
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        uniformLog(() -> delegateLogger.trace(marker, format, argArray));
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        uniformLog(() -> delegateLogger.trace(marker, msg, t));
    }


    // -------------------------------------- DEBUG 级别日志 ----------------------------------------------


    @Override
    public boolean isDebugEnabled() {
        return delegateLogger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        uniformLog(() -> delegateLogger.debug(msg));
    }

    @Override
    public void debug(String format, Object arg) {
        uniformLog(() -> delegateLogger.debug(format, arg));
    }


    @Override
    public void debug(String format, Object arg1, Object arg2) {
        uniformLog(() -> delegateLogger.debug(format, arg1, arg2));
    }


    @Override
    public void debug(String format, Object... arguments) {
        uniformLog(() -> delegateLogger.debug(format, arguments));
    }


    @Override
    public void debug(String msg, Throwable t) {
        uniformLog(() -> delegateLogger.debug(msg, t));
    }


    @Override
    public boolean isDebugEnabled(Marker marker) {
        return delegateLogger.isDebugEnabled(marker);
    }


    @Override
    public void debug(Marker marker, String msg) {
        uniformLog(() -> delegateLogger.debug(marker, msg));
    }


    @Override
    public void debug(Marker marker, String format, Object arg) {
        uniformLog(() -> delegateLogger.debug(marker, format, arg));
    }


    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        uniformLog(() -> delegateLogger.debug(marker, format, arg1, arg2));
    }


    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        uniformLog(() -> delegateLogger.debug(marker, format, arguments));
    }


    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        uniformLog(() -> delegateLogger.debug(marker, msg, t));
    }

    // -------------------------------------- INFO 级别日志 -----------------------------------------


    @Override
    public boolean isInfoEnabled() {
        return delegateLogger.isInfoEnabled();
    }


    @Override
    public void info(String msg) {
        uniformLog(() -> delegateLogger.info(msg));
    }


    @Override
    public void info(String format, Object arg) {
        uniformLog(() -> delegateLogger.info(format, arg));
    }


    @Override
    public void info(String format, Object arg1, Object arg2) {
        uniformLog(() -> delegateLogger.info(format, arg1, arg2));
    }


    @Override
    public void info(String format, Object... arguments) {
        uniformLog(() -> delegateLogger.info(format, arguments));
    }


    @Override
    public void info(String msg, Throwable t) {
        uniformLog(() -> delegateLogger.info(msg, t));
    }


    @Override
    public boolean isInfoEnabled(Marker marker) {
        return delegateLogger.isInfoEnabled(marker);
    }


    @Override
    public void info(Marker marker, String msg) {
        uniformLog(() -> delegateLogger.info(marker, msg));
    }


    @Override
    public void info(Marker marker, String format, Object arg) {
        uniformLog(() -> delegateLogger.info(marker, format, arg));
    }


    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        uniformLog(() -> delegateLogger.info(marker, format, arg1, arg2));
    }


    @Override
    public void info(Marker marker, String format, Object... arguments) {
        uniformLog(() -> delegateLogger.info(marker, format, arguments));
    }


    @Override
    public void info(Marker marker, String msg, Throwable t) {
        uniformLog(() -> delegateLogger.info(marker, msg, t));
    }


    // ---------------------------------------- WARN 级别日志 -------------------------------------------------


    @Override
    public boolean isWarnEnabled() {
        return delegateLogger.isWarnEnabled();
    }


    @Override
    public void warn(String msg) {
        uniformLog(() -> delegateLogger.warn(msg));
    }


    @Override
    public void warn(String format, Object arg) {
        uniformLog(() -> delegateLogger.warn(format, arg));
    }


    @Override
    public void warn(String format, Object... arguments) {
        uniformLog(() -> delegateLogger.warn(format, arguments));
    }


    @Override
    public void warn(String format, Object arg1, Object arg2) {
        uniformLog(() -> delegateLogger.warn(format, arg1, arg2));
    }


    @Override
    public void warn(String msg, Throwable t) {
        if (t instanceof ErrorCode) {
            warnWithErrorCode(((ErrorCode) t).getCode(), generateDetail((ErrorCode) t), t);
            return;
        }
        uniformLog(() -> delegateLogger.warn(msg, t));
    }


    @Override
    public boolean isWarnEnabled(Marker marker) {
        return delegateLogger.isWarnEnabled(marker);
    }


    @Override
    public void warn(Marker marker, String msg) {
        uniformLog(() -> delegateLogger.warn(marker, msg));
    }


    @Override
    public void warn(Marker marker, String format, Object arg) {
        uniformLog(() -> delegateLogger.warn(marker, format, arg));
    }


    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        uniformLog(() -> delegateLogger.warn(marker, format, arg1, arg2));
    }


    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        uniformLog(() -> delegateLogger.warn(marker, format, arguments));
    }


    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        uniformLog(() -> delegateLogger.warn(marker, msg, t));
    }

    // --------- 带错误码的 WARN ---------

    @Override
    public void log(ErrorCode errorCode) {
        Level level = errorCode.getLogLevel();
        // switch 没有分支预测，因此按照使用频率排序（注意并不是错误码定义的级别越多越频繁）
        switch (level) {
            case WARN:
                warn(errorCode);
                break;
            case INFO:
                info(errorCode);
                break;
            case ERROR:
                error(errorCode);
                break;
            case DEBUG:
                debug(errorCode);
                break;
            // shoulder 的默认实现中忽略了 trace 级别日志
            //case TRACE:

            default:
                break;
        }
    }


    // ----------------------------------- debug --------------------------------------

    @Override
    public void debug(ErrorCode errorCode) {
        uniformLog(errorCode.getCode(), () -> {
            if (errorCode instanceof Throwable) {
                delegateLogger.debug(generateDetail(errorCode), (Throwable) errorCode);
            } else {
                delegateLogger.debug(generateDetail(errorCode));
            }
        });
    }


    @Override
    public void debug(ErrorCode errorCode, Throwable t) {
        uniformLog(errorCode.getCode(), () -> delegateLogger.debug(generateDetail(errorCode), t));
    }


    @Override
    public void debugWithErrorCode(String errorCode, String msg) {
        uniformLog(errorCode, () -> delegateLogger.debug(msg));
    }


    @Override
    public void debugWithErrorCode(String errorCode, String format, Object arg) {
        uniformLog(errorCode, () -> delegateLogger.debug(format, arg));
    }


    @Override
    public void debugWithErrorCode(String errorCode, String format, Object... arguments) {
        uniformLog(errorCode, () -> delegateLogger.debug(format, arguments));

    }


    @Override
    public void debugWithErrorCode(String errorCode, String format, Object arg1, Object arg2) {
        uniformLog(errorCode, () -> delegateLogger.debug(format, arg1, arg2));
    }


    @Override
    public void debugWithErrorCode(String errorCode, String msg, Throwable t) {
        uniformLog(errorCode, () -> delegateLogger.debug(msg, t));
    }


    // ----------------------------------- info --------------------------------------

    @Override
    public void info(ErrorCode errorCode) {
        uniformLog(errorCode.getCode(), () -> {
            if (errorCode instanceof Throwable) {
                delegateLogger.info(generateDetail(errorCode), (Throwable) errorCode);
            } else {
                delegateLogger.info(generateDetail(errorCode));
            }
        });
    }

    @Override
    public void info(ErrorCode errorCode, Throwable t) {
        uniformLog(errorCode.getCode(), () -> delegateLogger.info(generateDetail(errorCode), t));
    }


    @Override
    public void infoWithErrorCode(String errorCode, String msg) {
        uniformLog(errorCode, () -> delegateLogger.info(msg));
    }


    @Override
    public void infoWithErrorCode(String errorCode, String format, Object arg) {
        uniformLog(errorCode, () -> delegateLogger.info(format, arg));
    }


    @Override
    public void infoWithErrorCode(String errorCode, String format, Object... arguments) {
        uniformLog(errorCode, () -> delegateLogger.info(format, arguments));

    }


    @Override
    public void infoWithErrorCode(String errorCode, String format, Object arg1, Object arg2) {
        uniformLog(errorCode, () -> delegateLogger.info(format, arg1, arg2));
    }


    @Override
    public void infoWithErrorCode(String errorCode, String msg, Throwable t) {
        uniformLog(errorCode, () -> delegateLogger.info(msg, t));
    }

    // ----------------------------------- warn --------------------------------------

    @Override
    public void warn(ErrorCode errorCode) {
        uniformLog(errorCode.getCode(), () -> {
            if (errorCode instanceof Throwable) {
                delegateLogger.warn(generateDetail(errorCode), (Throwable) errorCode);
            } else {
                delegateLogger.warn(generateDetail(errorCode));
            }
        });
    }

    @Override
    public void warn(ErrorCode errorCode, Throwable t) {
        uniformLog(errorCode.getCode(), () -> delegateLogger.warn(generateDetail(errorCode), t));
    }


    @Override
    public void warnWithErrorCode(String errorCode, String msg) {
        uniformLog(errorCode, () -> delegateLogger.warn(msg));
    }


    @Override
    public void warnWithErrorCode(String errorCode, String format, Object arg) {
        uniformLog(errorCode, () -> delegateLogger.warn(format, arg));
    }


    @Override
    public void warnWithErrorCode(String errorCode, String format, Object... arguments) {
        uniformLog(errorCode, () -> delegateLogger.warn(format, arguments));

    }


    @Override
    public void warnWithErrorCode(String errorCode, String format, Object arg1, Object arg2) {
        uniformLog(errorCode, () -> delegateLogger.warn(format, arg1, arg2));
    }


    @Override
    public void warnWithErrorCode(String errorCode, String msg, Throwable t) {
        uniformLog(errorCode, () -> delegateLogger.warn(msg, t));
    }

    // ---------------------------------------- ERROR 级别日志 ---------------------------------------------------

    @Override
    public boolean isErrorEnabled() {
        return delegateLogger.isErrorEnabled();
    }


    @Override
    public void error(String msg) {
        uniformLog(() -> delegateLogger.error(msg));
    }


    @Override
    public void error(String format, Object arg) {
        uniformLog(() -> delegateLogger.error(format, arg));
    }


    @Override
    public void error(String format, Object arg1, Object arg2) {
        uniformLog(() -> delegateLogger.error(format, arg1, arg2));
    }


    @Override
    public void error(String format, Object... arguments) {
        uniformLog(() -> delegateLogger.error(format, arguments));
    }


    @Override
    public void error(String msg, Throwable t) {
        if (t instanceof ErrorCode) {
            errorWithErrorCode(((ErrorCode) t).getCode(), generateDetail((ErrorCode) t), t);
            return;
        }
        uniformLog(() -> delegateLogger.error(msg, t));
    }


    @Override
    public boolean isErrorEnabled(Marker marker) {
        return delegateLogger.isErrorEnabled(marker);
    }


    @Override
    public void error(Marker marker, String msg) {
        uniformLog(() -> delegateLogger.error(marker, msg));
    }


    @Override
    public void error(Marker marker, String format, Object arg) {
        uniformLog(() -> delegateLogger.error(marker, format, arg));
    }


    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        uniformLog(() -> delegateLogger.error(marker, format, arg1, arg2));
    }


    @Override
    public void error(Marker marker, String format, Object... arguments) {
        uniformLog(() -> delegateLogger.error(marker, format, arguments));
    }


    @Override
    public void error(Marker marker, String msg, Throwable t) {
        uniformLog(() -> delegateLogger.error(marker, msg, t));
    }

    // --------- 带错误码的 ERROR ---------

    @Override
    public void error(ErrorCode errorCode) {
        uniformLog(errorCode.getCode(), () -> {
            if (errorCode instanceof Throwable) {
                delegateLogger.error(generateDetail(errorCode), (Throwable) errorCode);
            } else {
                delegateLogger.error(generateDetail(errorCode));
            }
        });
    }

    @Override
    public void error(ErrorCode errorCode, Throwable t) {
        uniformLog(errorCode.getCode(), () -> delegateLogger.error(generateDetail(errorCode), t));
    }

    @Override
    public void errorWithErrorCode(String errorCode, String msg) {
        uniformLog(errorCode, () -> delegateLogger.error(msg));
    }


    @Override
    public void errorWithErrorCode(String errorCode, String format, Object arg) {
        uniformLog(errorCode, () -> delegateLogger.error(format, arg));
    }


    @Override
    public void errorWithErrorCode(String errorCode, String format, Object arg1, Object arg2) {
        uniformLog(errorCode, () -> delegateLogger.error(format, arg1, arg2));
    }


    @Override
    public void errorWithErrorCode(String errorCode, String format, Object... arguments) {
        uniformLog(errorCode, () -> delegateLogger.error(format, arguments));
    }


    @Override
    public void errorWithErrorCode(String errorCode, String msg, Throwable t) {
        uniformLog(errorCode, () -> delegateLogger.error(msg, t));
    }


    // =============================================================================

    /**
     * 生成错误详情
     *
     * @return 填充参数后的 msg
     */
    private String generateDetail(ErrorCode errorCode) {
        return ExceptionUtil.generateExceptionMessage(errorCode.getMessage(), errorCode.getArgs());
    }

    /**
     * 统一格式打印日志
     * 在 slf4j 基础上封装一点信息，如调用链信息
     *
     * @param logger 第三方日志
     */
    private void uniformLog(GeneralLogger logger) {
        String traceInfo = generateTraceInfo();
        if (traceInfo != null) {
            MDC.put(MDC_TRACE_NAME, traceInfo);
        }
        logger.log();
        if (traceInfo != null) {
            MDC.remove(MDC_TRACE_NAME);
        }
    }

    /**
     * 统一格式打印日志
     * 在 slf4j 基础上封装一点信息，如调用链信息、错误码
     *
     * @param logger 第三方日志
     */
    private void uniformLog(String errorCode, GeneralLogger logger) {
        // 校验 errorCode
        if (org.shoulder.core.util.StringUtils.isEmpty(errorCode)) {
            uniformLog(logger);
            return;
        }
        addErrorCodeInfo(errorCode);
        uniformLog(logger);
        cleanErrorCodeInfo();
    }

    /**
     * 统一打印错误码格式
     *
     * @param errorCode 错误码
     * @return [errorCodePrefix + errorCode]
     */
    private String generateErrorCode(String errorCode) {
        return ERROR_CODE_PREFIX + ExceptionUtil.formatErrorCode(errorCode) + ERROR_CODE_SUFFIX;
    }

    /**
     * 统一打印调用链信息
     *
     * @return <traceId,spanId>
     */
    private String generateTraceInfo() {
        String traceId = AppContext.getTraceId();
        if (StringUtils.isEmpty(traceId)) {
            return null;
        }
        // todo 支持 spanId
        String spanId = MDC.get(MDC_SPAN_ID);
        return TRACE_PREFIX + traceId +
            DELIMITER + (spanId == null ? "" : spanId) +
            TRACE_SUFFIX;
    }

    /**
     * 将错误码信息放到 MDC 中，以方便输出时填充
     *
     * @param errorCode 错误码
     */
    private void addErrorCodeInfo(String errorCode) {
        MDC.put(MDC_ERROR_CODE_NAME, generateErrorCode(errorCode));
    }

    /**
     * 清理错误码信息
     */
    private void cleanErrorCodeInfo() {
        MDC.remove(MDC_ERROR_CODE_NAME);
    }


    /**
     * 一般为第三方的日志记录器，如 logback 等
     */
    @FunctionalInterface
    interface GeneralLogger {

        /**
         * 记录日志
         */
        void log();
    }
}
