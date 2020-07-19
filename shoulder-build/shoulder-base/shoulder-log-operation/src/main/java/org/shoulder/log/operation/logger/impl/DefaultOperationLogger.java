package org.shoulder.log.operation.logger.impl;

import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.entity.OperationLogEntity;
import org.shoulder.log.operation.format.OperationLogFormatter;
import org.shoulder.log.operation.intercept.OperationLoggerInterceptor;
import org.shoulder.log.operation.logger.OperationLogValidator;
import org.shoulder.log.operation.logger.OperationLogger;
import org.shoulder.log.operation.util.OperationLogBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * 操作日志记录
 * 记录前先校验，若日志不符合自己的规范要求则不记录这条操作日志，并在运行日志中记录为什么不合规
 *
 * @author lym
 */
public class DefaultOperationLogger implements OperationLogger {

    private static final Logger log = LoggerFactory.getLogger(DefaultOperationLogger.class);

    /**
     * logback.xml 中用于记录操作日志的 logger 名称
     */
    private static final String LOGBACK_XML_OPERATION_LOGGER_NAME = "OPERATION_LOGGER";

    private static final Logger opLogger = initOperationLogger();

    private final OperationLogFormatter operationLogFormatter;

    private final OperationLogValidator opLogValidator = new DefaultOperationLogValidator();

    /**
     * 日志拦截器
     */
    private Collection<OperationLoggerInterceptor> logInterceptors = new LinkedList<>();

    public DefaultOperationLogger(OperationLogFormatter operationLogFormatter) {
        this.operationLogFormatter = operationLogFormatter;
    }

    /**
     * 记录一条操作日志
     */
    @Override
    public void log(OperationLogEntity opLogEntity) {
        try {
            beforeLog(opLogEntity);
            opLogger.info(operationLogFormatter.format(opLogEntity));
            afterLog(opLogEntity);
        } catch (Exception e) {
            // 当抛出异常先进行处理
            //afterValidateFail();
            log.warn("logEntity is not qualified! -- " + e.getMessage() + operationLogFormatter.format(opLogEntity), e);

        }
    }

    /**
     * 记录多条操作日志
     */
    @Override
    public void log(@NonNull Collection<? extends OperationLogEntity> opLogEntityList) {
        // 如果过多，需要考虑多线程
        opLogEntityList.forEach(this::log);
    }

    /**
     * 拼装记录多条操作日志
     */
    @Override
    public void log(@NonNull OperationLogEntity opLogEntity, List<? extends Operable> operableList) {
        // 组装前
        operableList = beforeAssembleBatchLogs(opLogEntity, operableList);
        // 组装批量操作日志
        List<? extends OperationLogEntity> opLogs = OperationLogBuilder.newLogsFrom(opLogEntity, operableList);
        // 组装后
        opLogs = afterAssembleBatchLogs(opLogs);

        // 如果过多，需要考虑多线程
        opLogs.forEach(this::log);
    }

    // **************************** 监听器相关 ******************************

    private List<? extends Operable> beforeAssembleBatchLogs(OperationLogEntity template, List<? extends Operable> operableCollection) {
        List<? extends Operable> result = operableCollection;
        for (OperationLoggerInterceptor interceptor : logInterceptors) {
            result = interceptor.beforeAssembleBatchLogs(template, result);
        }
        return result;
    }

    private List<? extends OperationLogEntity> afterAssembleBatchLogs(List<? extends OperationLogEntity> operationLogEntities) {
        List<? extends OperationLogEntity> result = operationLogEntities;
        for (OperationLoggerInterceptor interceptor : logInterceptors) {
            result = interceptor.afterAssembleBatchLogs(result);
        }
        return result;
    }


    private void beforeLog(OperationLogEntity opLogEntity) {
        logInterceptors.forEach(listener -> listener.beforeLog(opLogEntity));
    }

    /*private void afterValidateFail(OperationLogEntity opLogEntity){
        logInterceptors.forEach(listener -> listener.afterValidateFail(opLogEntity));
    }*/

    private void afterLog(OperationLogEntity opLogEntity) {
        logInterceptors.forEach(listener -> listener.afterLog(opLogEntity));
    }

    @Override
    public void addInterceptor(OperationLoggerInterceptor logInterceptor) {
        logInterceptors.add(logInterceptor);
        if (log.isDebugEnabled()) {
            log.debug("add a OperationLogInterceptor: " + logInterceptor.getClass().getName());
        }
    }

    // **************************** 初始化 logger ******************************

    private static Logger initOperationLogger() {
        return getOperationLogger(LOGBACK_XML_OPERATION_LOGGER_NAME);
    }

    private static Logger getOperationLogger(final String loggerName) {
        Logger logger = LoggerFactory.getLogger(loggerName);
        if (logger != null) {
            return logger;
        }
        throw new RuntimeException("No OperationLogger named " + loggerName + " in LoggerFactory! " +
                "Please check if there were any logger that name='" + loggerName + "' in your " +
                "logback.xml.");
    }
}
