package org.shoulder.log.operation.logger.impl;

import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.entity.OperationLogEntity;
import org.shoulder.log.operation.intercept.OperationLoggerInterceptor;
import org.shoulder.log.operation.logger.OperationLogger;
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
    public void log(OperationLogEntity opLogEntity) {
        executor.execute(() -> delegate.log(opLogEntity));
    }

    /**
     * 记录多条操作日志
     */
    @Override
    public void log(@NonNull Collection<? extends OperationLogEntity> opLogEntityList) {
        executor.execute(() -> delegate.log(opLogEntityList));
    }

    @Override
    public void log(@NonNull OperationLogEntity opLogEntity, List<? extends Operable> operableList) {
        executor.execute(() -> delegate.log(opLogEntity, operableList));
    }

    @Override
    public void addInterceptor(OperationLoggerInterceptor logInterceptor) {
        this.delegate.addInterceptor(logInterceptor);
    }

    public AsyncOperationLogger setLogger(@NonNull OperationLogger delegate) {
        this.delegate = delegate;
        return this;
    }

    public AsyncOperationLogger setExecutor(@NonNull Executor executor) {
        this.executor = executor;
        return this;
    }

    public OperationLogger getDelegate() {
        return delegate;
    }

    public Executor getExecutor() {
        return executor;
    }
}
