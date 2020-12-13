package org.shoulder.log.operation.logger.impl;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.log.operation.dto.OperationLogDTO;
import org.shoulder.log.operation.format.OperationLogFormatter;
import org.shoulder.log.operation.logger.AbstractOperationLogger;
import org.shoulder.log.operation.logger.OperationLogger;

/**
 * 以日志文件记录操作日志，并以 {@link OperationLogFormatter} 作为日志格式。后续可由日志采集器收集、处理、转发至日志中心。
 * <p>
 * 注意：使用前需要保证 logback.xml 中 loggerName 对应的日志记录器存在。
 *
 * @author lym
 */
public class LogOperationLogger extends AbstractOperationLogger implements OperationLogger {

    private static final Logger log = LoggerFactory.getLogger(LogOperationLogger.class);

    /**
     * logback.xml / log4j.xml 中用于记录操作日志的 logger 的名称 默认值
     */
    private static final String DEFAULT_OPERATION_LOGGER_NAME = "OPERATION_LOGGER";

    private final OperationLogFormatter operationLogFormatter;

    private final Logger opLogger;

    public LogOperationLogger(OperationLogFormatter operationLogFormatter) {
        this(operationLogFormatter, DEFAULT_OPERATION_LOGGER_NAME);
    }

    public LogOperationLogger(OperationLogFormatter operationLogFormatter, String loggerName) {
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
