package org.shoulder.core.log;

import org.shoulder.core.exception.ErrorCode;
import org.shoulder.core.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.springframework.util.StringUtils;

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
     * todo 使用 spring cloud sleuth 中定义的？还是在shoulder中定义？ B3Propagation
     */
    private static final String MDC_TRACE_NAME = "S-TRACE";

    // MDC key

    private static final String MDC_PARENT_ID = "parent_id";
    private static final String MDC_TRACE_ID = "trace_id";
    private static final String MDC_SPAN_ID = "span_id";

    // 定义前后缀，以便于日志分割和分析

    private static final String SPACE = " ";

    private static final String ERROR_CODE_PREFIX = "[";
    private static final String ERROR_CODE_SUFFIX = "]";

    private static final String TRACE_PREFIX = "<";
    private static final String DELIMITER = ",";
    private static final String TRACE_SUFFIX = ">";

    /**
     * Slf4j 的 logger
     */
    protected final Logger log;

    public ShoulderLogger(Class<?> clazz) {
        log = LoggerFactory.getLogger(clazz);
    }

    public ShoulderLogger(String name) {
        log = LoggerFactory.getLogger(name);
    }

    @Override
    public String getName() {
        return log.getName();
    }

    // -------------------------------------- TRACE 级别日志 -------------------------------------------

    @Override
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        uniformLog(() -> log.trace(msg));
    }

    @Override
    public void trace(String format, Object arg) {
        uniformLog(() -> log.trace(format, arg));
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        uniformLog(() -> log.trace(format, arg1, arg2));
    }

    @Override
    public void trace(String format, Object... arguments) {
        uniformLog(() -> log.trace(format, arguments));
    }

    @Override
    public void trace(String msg, Throwable t) {
        uniformLog(() -> log.trace(msg, t));
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return log.isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String msg) {
        uniformLog(() -> log.trace(marker, msg));
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        uniformLog(() -> log.trace(marker, format, arg));
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        uniformLog(() -> log.trace(marker, format, arg1, arg2));
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        uniformLog(() -> log.trace(marker, format, argArray));
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        uniformLog(() -> log.trace(marker, msg, t));
    }


    // -------------------------------------- DEBUG 级别日志 ----------------------------------------------


    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        uniformLog(() -> log.debug(msg));
    }

    @Override
    public void debug(String format, Object arg) {
        uniformLog(() -> log.debug(format, arg));
    }


    @Override
    public void debug(String format, Object arg1, Object arg2) {
        uniformLog(() -> log.debug(format, arg1, arg2));
    }


    @Override
    public void debug(String format, Object... arguments) {
        uniformLog(() -> log.debug(format, arguments));
    }


    @Override
    public void debug(String msg, Throwable t) {
        uniformLog(() -> log.debug(msg, t));
    }


    @Override
    public boolean isDebugEnabled(Marker marker) {
        return log.isDebugEnabled(marker);
    }


    @Override
    public void debug(Marker marker, String msg) {
        uniformLog(() -> log.debug(marker, msg));
    }


    @Override
    public void debug(Marker marker, String format, Object arg) {
        uniformLog(() -> log.debug(marker, format, arg));
    }


    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        uniformLog(() -> log.debug(marker, format, arg1, arg2));
    }


    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        uniformLog(() -> log.debug(marker, format, arguments));
    }


    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        uniformLog(() -> log.debug(marker, msg, t));
    }

    // -------------------------------------- INFO 级别日志 -----------------------------------------


    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }


    @Override
    public void info(String msg) {
        uniformLog(() -> log.info(msg));
    }


    @Override
    public void info(String format, Object arg) {
        uniformLog(() -> log.info(format, arg));
    }


    @Override
    public void info(String format, Object arg1, Object arg2) {
        uniformLog(() -> log.info(format, arg1, arg2));
    }


    @Override
    public void info(String format, Object... arguments) {
        uniformLog(() -> log.info(format, arguments));
    }


    @Override
    public void info(String msg, Throwable t) {
        uniformLog(() -> log.info(msg, t));
    }


    @Override
    public boolean isInfoEnabled(Marker marker) {
        return log.isInfoEnabled(marker);
    }


    @Override
    public void info(Marker marker, String msg) {
        uniformLog(() -> log.info(marker, msg));
    }


    @Override
    public void info(Marker marker, String format, Object arg) {
        uniformLog(() -> log.info(marker, format, arg));
    }


    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        uniformLog(() -> log.info(marker, format, arg1, arg2));
    }


    @Override
    public void info(Marker marker, String format, Object... arguments) {
        uniformLog(() -> log.info(marker, format, arguments));
    }


    @Override
    public void info(Marker marker, String msg, Throwable t) {
        uniformLog(() -> log.info(marker, msg, t));
    }


    // ---------------------------------------- WARN 级别日志 -------------------------------------------------


    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }


    @Override
    public void warn(String msg) {
        uniformLog(() -> log.warn(msg));
    }


    @Override
    public void warn(String format, Object arg) {
        uniformLog(() -> log.warn(format, arg));
    }


    @Override
    public void warn(String format, Object... arguments) {
        uniformLog(() -> log.warn(format, arguments));
    }


    @Override
    public void warn(String format, Object arg1, Object arg2) {
        uniformLog(() -> log.warn(format, arg1, arg2));
    }


    @Override
    public void warn(String msg, Throwable t) {
        if (t instanceof ErrorCode) {
            warnWithErrorCode(((ErrorCode) t).getCode(), ((ErrorCode) t).generateDetail(), t);
            return;
        }
        uniformLog(() -> log.warn(msg, t));
    }


    @Override
    public boolean isWarnEnabled(Marker marker) {
        return log.isWarnEnabled(marker);
    }


    @Override
    public void warn(Marker marker, String msg) {
        uniformLog(() -> log.warn(marker, msg));
    }


    @Override
    public void warn(Marker marker, String format, Object arg) {
        uniformLog(() -> log.warn(marker, format, arg));
    }


    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        uniformLog(() -> log.warn(marker, format, arg1, arg2));
    }


    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        uniformLog(() -> log.warn(marker, format, arguments));
    }


    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        uniformLog(() -> log.warn(marker, msg, t));
    }

    // --------- 带错误码的 WARN ---------

    @Override
    public void warn(ErrorCode error) {
        uniformLog(error.getCode(), () -> {
            if (error instanceof Throwable) {
                log.warn(error.generateDetail(), (Throwable) error);
            } else {
                log.warn(error.generateDetail());
            }
        });
    }


    @Override
    public void warnWithErrorCode(String errorCode, String msg) {
        uniformLog(errorCode, () -> log.warn(msg));
    }


    @Override
    public void warnWithErrorCode(String errorCode, String format, Object arg) {
        uniformLog(errorCode, () -> log.warn(format, arg));
    }


    @Override
    public void warnWithErrorCode(String errorCode, String format, Object... arguments) {
        uniformLog(errorCode, () -> log.warn(format, arguments));

    }


    @Override
    public void warnWithErrorCode(String errorCode, String format, Object arg1, Object arg2) {
        uniformLog(errorCode, () -> log.warn(format, arg1, arg2));
    }


    @Override
    public void warnWithErrorCode(String errorCode, String msg, Throwable t) {
        uniformLog(errorCode, () -> log.warn(msg, t));
    }

    // ---------------------------------------- ERROR 级别日志 ---------------------------------------------------

    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }


    @Override
    public void error(String msg) {
        uniformLog(() -> log.error(msg));
    }


    @Override
    public void error(String format, Object arg) {
        uniformLog(() -> log.error(format, arg));
    }


    @Override
    public void error(String format, Object arg1, Object arg2) {
        uniformLog(() -> log.error(format, arg1, arg2));
    }


    @Override
    public void error(String format, Object... arguments) {
        uniformLog(() -> log.error(format, arguments));
    }


    @Override
    public void error(String msg, Throwable t) {
        if (t instanceof ErrorCode) {
            errorWithErrorCode(((ErrorCode) t).getCode(), ((ErrorCode) t).generateDetail(), t);
            return;
        }
        uniformLog(() -> log.error(msg, t));
    }


    @Override
    public boolean isErrorEnabled(Marker marker) {
        return log.isErrorEnabled(marker);
    }


    @Override
    public void error(Marker marker, String msg) {
        uniformLog(() -> log.error(marker, msg));
    }


    @Override
    public void error(Marker marker, String format, Object arg) {
        uniformLog(() -> log.error(marker, format, arg));
    }


    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        uniformLog(() -> log.error(marker, format, arg1, arg2));
    }


    @Override
    public void error(Marker marker, String format, Object... arguments) {
        uniformLog(() -> log.error(marker, format, arguments));
    }


    @Override
    public void error(Marker marker, String msg, Throwable t) {
        uniformLog(() -> log.error(marker, msg, t));
    }

    // --------- 带错误码的 ERROR ---------

    @Override
    public void error(ErrorCode error) {
        uniformLog(error.getCode(), () -> {
            if (error instanceof Throwable) {
                log.error(error.generateDetail(), (Throwable) error);
            } else {
                log.error(error.generateDetail());
            }
        });
    }

    @Override
    public void errorWithErrorCode(String errorCode, String msg) {
        uniformLog(errorCode, () -> log.error(msg));
    }


    @Override
    public void errorWithErrorCode(String errorCode, String format, Object arg) {
        uniformLog(errorCode, () -> log.error(format, arg));
    }


    @Override
    public void errorWithErrorCode(String errorCode, String format, Object arg1, Object arg2) {
        uniformLog(errorCode, () -> log.error(format, arg1, arg2));
    }


    @Override
    public void errorWithErrorCode(String errorCode, String format, Object... arguments) {
        uniformLog(errorCode, () -> log.error(format, arguments));
    }


    @Override
    public void errorWithErrorCode(String errorCode, String msg, Throwable t) {
        uniformLog(errorCode, () -> log.error(msg, t));
    }


    // =============================================================================

    /**
     * 统一格式打印日志
     * 在第三方日志记录器的基础上封装一点信息，如调用链信息
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
     * 在第三方日志记录器的基础上封装一点信息，如调用链信息
     *
     * @param logger 第三方日志
     */
    private void uniformLog(String errorCode, GeneralLogger logger) {
        addErrorCodeInfo(errorCode);
        String traceInfo = generateTraceInfo();
        if (traceInfo != null) {
            MDC.put(MDC_TRACE_NAME, traceInfo);
        }
        logger.log();
        if (traceInfo != null) {
            MDC.remove(MDC_TRACE_NAME);
        }
        cleanErrorCodeInfo();
    }


    /**
     * 一般为第三方的日志记录器，如 logback 等
     */
    interface GeneralLogger {
        void log();
    }

    /**
     * 统一打印错误码格式
     *
     * @param errorCode 错误码
     * @return [errorCodePrefix + errorCode]
     */
    String generateErrorCode(String errorCode) {
        return SPACE + ERROR_CODE_PREFIX + ExceptionUtil.formatErrorCode(errorCode) + ERROR_CODE_SUFFIX;
    }

    /**
     * 统一打印调用链信息
     *
     * @return <traceId,spanId>
     */
    String generateTraceInfo() {
        String traceId = MDC.get(MDC_TRACE_ID);
        if (StringUtils.isEmpty(traceId)) {
            return null;
        }
        String spanId = MDC.get(MDC_SPAN_ID);
        return SPACE +
            TRACE_PREFIX + traceId +
            DELIMITER + (spanId == null ? "" : spanId) +
            TRACE_SUFFIX;
    }

    /**
     * 将错误码信息放到 MDC 中，以方便输出时填充
     *
     * @param errorCode 错误码
     */
    void addErrorCodeInfo(String errorCode) {
        MDC.put(MDC_ERROR_CODE_NAME, generateErrorCode(errorCode));
    }

    /**
     * 清理错误码信息
     */
    void cleanErrorCodeInfo() {
        MDC.remove(MDC_ERROR_CODE_NAME);
    }
}
