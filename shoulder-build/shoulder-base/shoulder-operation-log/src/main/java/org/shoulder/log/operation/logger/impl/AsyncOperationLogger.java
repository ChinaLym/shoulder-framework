package org.shoulder.log.operation.logger.impl;

import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.dto.OperationLogDTO;
import org.shoulder.log.operation.logger.OperationLogger;
import org.shoulder.log.operation.logger.intercept.OperationLoggerInterceptor;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * 异步操作日志记录器
 *
 * @author lym
 */
public class AsyncOperationLogger implements OperationLogger {

    private OperationLogger delegate;

    private Executor executor;

    /**
     * 记录一条操作日志
     */
    @Override
    public void log(OperationLogDTO opLog) {
        executor.execute(() -> delegate.log(opLog));
    }

    /**
     * 记录多条操作日志
     */
    @Override
    public void log(@NonNull Collection<? extends OperationLogDTO> opLogList) {
        executor.execute(() -> delegate.log(opLogList));
    }

    @Override
    public void log(@NonNull OperationLogDTO opLog, List<? extends Operable> operableList) {
        executor.execute(() -> delegate.log(opLog, operableList));
    }

    @Override
    public void addInterceptor(OperationLoggerInterceptor logInterceptor) {
        this.delegate.addInterceptor(logInterceptor);
    }

    public AsyncOperationLogger setLogger(@NonNull OperationLogger delegate) {
        this.delegate = delegate;
        return this;
    }

    public OperationLogger getDelegate() {
        return delegate;
    }

    public Executor getExecutor() {
        return executor;
    }

    public AsyncOperationLogger setExecutor(@NonNull Executor executor) {
        this.executor = executor;
        return this;
    }
}
