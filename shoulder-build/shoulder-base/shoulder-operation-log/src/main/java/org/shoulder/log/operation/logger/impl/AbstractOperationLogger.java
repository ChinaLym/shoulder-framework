package org.shoulder.log.operation.logger.impl;

import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.dto.OperationLogDTO;
import org.shoulder.log.operation.logger.OperationLogger;
import org.shoulder.log.operation.logger.intercept.OperationLoggerInterceptor;
import org.shoulder.log.operation.util.OperationLogBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * 基础操作日志记录，封装了记录前后触发逻辑
 * 记录前先校验，若日志不符合自己的规范要求则不记录这条操作日志，并在运行日志中记录为什么不合规
 *
 * @author lym
 */
public abstract class AbstractOperationLogger implements OperationLogger {

    private static final Logger log = LoggerFactory.getLogger(AbstractOperationLogger.class);

    /**
     * 日志拦截器
     */
    private Collection<OperationLoggerInterceptor> logInterceptors = new LinkedList<>();

    /**
     * 记录一条操作日志
     */
    @Override
    public void log(OperationLogDTO opLog) {
        try {
            beforeLog(opLog);
            doLog(opLog);
            afterLog(opLog);
        } catch (Exception e) {
            //afterValidateFail();
            handleLogException(e, opLog);
        }
    }

    /**
     * 记录多条操作日志
     */
    @Override
    public void log(@NonNull Collection<? extends OperationLogDTO> opLogList) {
        // 如果过多，需要考虑多线程
        opLogList.forEach(this::log);
    }

    /**
     * 拼装记录多条操作日志
     */
    @Override
    public void log(@NonNull OperationLogDTO opLog, List<? extends Operable> operableList) {
        // 组装前
        operableList = beforeAssembleBatchLogs(opLog, operableList);
        // 组装批量操作日志
        List<? extends OperationLogDTO> opLogs = OperationLogBuilder.newLogsFrom(opLog, operableList);
        // 组装后
        opLogs = afterAssembleBatchLogs(opLogs);

        // 如果过多，需要考虑多线程
        opLogs.forEach(this::log);
    }


    /**
     * 子类需要实现具体如何记录日志
     *
     * @param opLog 需要记录日志的实体
     */
    protected abstract void doLog(OperationLogDTO opLog);

    /**
     * 默认记录一条warn日志，但子类可以覆盖当记录日志时出现异常如何处理
     *
     * @param e     具体是什么异常
     * @param opLog 需要记录日志的实体
     */
    protected void handleLogException(Exception e, OperationLogDTO opLog) {
        log.warn("Log is not qualified! -- " + e.getMessage() + opLog, e);
    }

    // **************************** 监听器相关 ******************************

    private List<? extends Operable> beforeAssembleBatchLogs(OperationLogDTO template, List<? extends Operable> operableCollection) {
        List<? extends Operable> result = operableCollection;
        for (OperationLoggerInterceptor interceptor : logInterceptors) {
            result = interceptor.beforeAssembleBatchLogs(template, result);
        }
        return result;
    }

    private List<? extends OperationLogDTO> afterAssembleBatchLogs(List<? extends OperationLogDTO> operationLogEntities) {
        List<? extends OperationLogDTO> result = operationLogEntities;
        for (OperationLoggerInterceptor interceptor : logInterceptors) {
            result = interceptor.afterAssembleBatchLogs(result);
        }
        return result;
    }


    private void beforeLog(OperationLogDTO opLog) {
        logInterceptors.forEach(listener -> listener.beforeLog(opLog));
    }

    /*private void afterValidateFail(OperationLog opLog){
        logInterceptors.forEach(listener -> listener.afterValidateFail(opLog));
    }*/

    private void afterLog(OperationLogDTO opLog) {
        logInterceptors.forEach(listener -> listener.afterLog(opLog));
    }

    @Override
    public void addInterceptor(OperationLoggerInterceptor logInterceptor) {
        logInterceptors.add(logInterceptor);
        if (log.isDebugEnabled()) {
            log.debug("add a OperationLogInterceptor: " + logInterceptor.getClass().getName());
        }
    }

}
