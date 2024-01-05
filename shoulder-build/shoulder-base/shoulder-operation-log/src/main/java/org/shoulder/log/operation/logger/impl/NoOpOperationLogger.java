package org.shoulder.log.operation.logger.impl;

import jakarta.annotation.Nonnull;
import org.shoulder.core.model.Operable;
import org.shoulder.log.operation.logger.OperationLogger;
import org.shoulder.log.operation.logger.OperationLoggerInterceptor;
import org.shoulder.log.operation.model.OperationLogDTO;

import java.util.Collection;
import java.util.List;

/**
 * 关掉操作日志
 *
 * @author lym
 */
public class NoOpOperationLogger implements OperationLogger {

    @Override
    public void log(@Nonnull OperationLogDTO opLog) {

    }

    @Override
    public void log(@Nonnull Collection<? extends OperationLogDTO> opLogList) {

    }

    @Override
    public void log(@Nonnull OperationLogDTO opLog, List<? extends Operable> operableList) {

    }

    @Override
    public void addInterceptor(OperationLoggerInterceptor logInterceptor) {

    }
}
