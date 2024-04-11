package org.shoulder.log.operation.logger;

import jakarta.annotation.Nonnull;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.model.Operable;
import org.shoulder.log.operation.context.OperationLogFactory;
import org.shoulder.log.operation.model.OperationLogDTO;
import org.slf4j.Logger;

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

    protected final Logger log = ShoulderLoggers.SHOULDER_DEFAULT;

    /**
     * 日志拦截器
     */
    private final Collection<OperationLoggerInterceptor> logInterceptors = new LinkedList<>();

    /**
     * 记录一条操作日志
     */
    @Override
    public void log(@Nonnull OperationLogDTO opLog) {
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
     * 【性能】默认单条循环，扩展时可考虑覆盖默认实现，支持真正的批量操作
     * 【扩展】自行实现时，也可自己定义批量记录前后置处理器
     */
    @Override
    public void log(@Nonnull Collection<? extends OperationLogDTO> opLogList) {
        // 如果过多，需要考虑多线程
        opLogList.forEach(this::log);
    }

    /**
     * 拼装循环记录多条操作日志
     */
    @Override
    public void log(@Nonnull OperationLogDTO opLog, List<? extends Operable> operableList) {
        // 组装前
        operableList = beforeAssembleBatchLogs(opLog, operableList);
        // 组装批量操作日志
        List<? extends OperationLogDTO> opLogs = OperationLogFactory.createFromTemplate(opLog, operableList);
        // 组装后
        opLogs = afterAssembleBatchLogs(opLogs);

        // 批量记录
        log(opLogs);
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
        if (ShoulderLoggers.SHOULDER_CONFIG.isDebugEnabled()) {
            ShoulderLoggers.SHOULDER_CONFIG.info("add a OperationLogInterceptor: " + logInterceptor.getClass().getName());
        }
    }

}
