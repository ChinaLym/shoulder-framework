package org.shoulder.log.operation.logger.impl;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.dto.OperationLogDTO;
import org.shoulder.log.operation.logger.OperationLogger;
import org.shoulder.log.operation.logger.intercept.OperationLoggerInterceptor;

import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * 异步操作日志记录器
 * 仅仅把任务放入线程池
 *
 * @author lym
 */
public class AsyncOperationLogger implements OperationLogger {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 日志记录器
     */
    private OperationLogger delegate;

    /**
     * 线程池
     */
    private ExecutorService executorService;

    /**
     * 记录一条操作日志
     */
    @Override
    public void log(OperationLogDTO opLog) {
        executorService.execute(() -> delegate.log(opLog));
    }

    /**
     * 记录多条操作日志
     */
    @Override
    public void log(@Nonnull Collection<? extends OperationLogDTO> opLogList) {
        executorService.execute(() -> delegate.log(opLogList));
    }

    @Override
    public void log(@Nonnull OperationLogDTO opLog, List<? extends Operable> operableList) {
        executorService.execute(() -> delegate.log(opLog, operableList));
    }

    @Override
    public void addInterceptor(OperationLoggerInterceptor logInterceptor) {
        this.delegate.addInterceptor(logInterceptor);
    }

    public AsyncOperationLogger setLogger(@Nonnull OperationLogger delegate) {
        this.delegate = delegate;
        return this;
    }

    public OperationLogger getDelegate() {
        return delegate;
    }

    public Executor getExecutorService() {
        return executorService;
    }

    public AsyncOperationLogger setExecutorService(@Nonnull ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    @PreDestroy
    public void preDestroy(){
        try {
            log.info("{} clean start...", getClass().getSimpleName());
            executorService.shutdown();
            log.info("{} clean finished.", getClass().getSimpleName());
        } catch (Exception e) {
            // on shutDown 钩子可能抛异常
            log.error(getClass().getSimpleName() + " clean FAIL! - ", e);
        }
    }

}
