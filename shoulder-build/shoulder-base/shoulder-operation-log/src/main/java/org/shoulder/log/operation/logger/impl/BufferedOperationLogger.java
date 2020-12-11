package org.shoulder.log.operation.logger.impl;

import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.dto.OperationLogDTO;
import org.shoulder.log.operation.logger.OperationLogger;
import org.shoulder.log.operation.logger.OperationLoggerInterceptor;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 带缓冲的操作日志记录器
 * 将日志放入队列中，当队列中日志达到一定数量 /
 * todo 等 logger 重构
 *
 * @author lym
 */
public class BufferedOperationLogger implements OperationLogger {

    private final ConcurrentLinkedQueue logBuffer = new ConcurrentLinkedQueue();

    /**
     * 日志记录器
     */
    private OperationLogger delegate;

    /**
     * 记录一条操作日志
     */
    @Override
    public void log(OperationLogDTO opLog) {
        logBuffer.add(null);
    }

    /**
     * 记录多条操作日志
     */
    @Override
    public void log(@Nonnull Collection<? extends OperationLogDTO> opLogList) {
        logBuffer.add(null);
    }

    @Override
    public void log(@Nonnull OperationLogDTO opLog, List<? extends Operable> operableList) {
        logBuffer.add(null);
    }

    @Override
    public void addInterceptor(OperationLoggerInterceptor logInterceptor) {
        this.delegate.addInterceptor(logInterceptor);
    }

    public BufferedOperationLogger setLogger(@Nonnull OperationLogger delegate) {
        this.delegate = delegate;
        return this;
    }

    public OperationLogger getDelegate() {
        return delegate;
    }


}
