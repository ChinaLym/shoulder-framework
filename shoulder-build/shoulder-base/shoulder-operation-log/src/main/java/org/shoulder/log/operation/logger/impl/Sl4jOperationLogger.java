package org.shoulder.log.operation.logger.impl;

import org.shoulder.log.operation.dto.OperationLogDTO;
import org.shoulder.log.operation.format.OperationLogFormatter;
import org.shoulder.log.operation.logger.OperationLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 以 Sl4j 的接口记录操作日志记录，并以 {@link OperationLogFormatter} 作为日志格式。后续由日志采集器收集、处理、转发至日志中心。
 * <p>
 * 注意：使用前需要保证 loggerName 对应的日志记录器存在。
 *
 * @author lym
 */
public class Sl4jOperationLogger extends AbstractOperationLogger implements OperationLogger {

    private static final Logger log = LoggerFactory.getLogger(Sl4jOperationLogger.class);

    /**
     * logback.xml / log4j.xml 中用于记录操作日志的 logger 的名称默认值
     */
    private static final String DEFAULT_OPERATION_LOGGER_NAME = "OPERATION_LOGGER";

    private final OperationLogFormatter operationLogFormatter;

    private final Logger opLogger;

    public Sl4jOperationLogger(OperationLogFormatter operationLogFormatter) {
        this(operationLogFormatter, DEFAULT_OPERATION_LOGGER_NAME);
    }

    public Sl4jOperationLogger(OperationLogFormatter operationLogFormatter, String loggerName) {
        this.operationLogFormatter = operationLogFormatter;
        this.opLogger = getOperationLogger(loggerName);
    }

    @Override
    protected void doLog(OperationLogDTO opLog) {
        opLogger.info(operationLogFormatter.format(opLog));
    }

    // **************************** 初始化 logger ******************************

    private Logger getOperationLogger(final String loggerName) {
        Logger logger = LoggerFactory.getLogger(loggerName);
        if (logger != null) {
            log.debug("use '{}' as opLogger", loggerName);
            return logger;
        }
        throw new RuntimeException("No OperationLogger named " + loggerName + " in LoggerFactory! " +
            "Please check if there were any logger that name='" + loggerName + "' in your " +
            "logback.xml.");
    }
}
