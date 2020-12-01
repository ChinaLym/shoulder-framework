package org.shoulder.log.operation.logger;

import org.shoulder.log.operation.context.OpLogContextHolder;
import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.dto.OperationLogDTO;
import org.shoulder.log.operation.logger.intercept.OperationLoggerInterceptor;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * 操作日志记录器
 * 子类要求：不能抛出异常，遇到异常需要 logger处理。因为抛出异常可能导致上层业务失败
 *
 * @author lym
 */
public interface OperationLogger {

    /**
     * 记录日志上下文中的日志实体内容 {@link OpLogContextHolder}
     */
    default void log() {
        OperationLogDTO opLog = OpLogContextHolder.getLog();
        if (opLog != null) {
            List<? extends Operable> operableCollection = OpLogContextHolder.getOperableObjects();

            if (operableCollection == null || operableCollection.isEmpty()) {
                this.log(opLog);
            } else {
                this.log(opLog, operableCollection);
            }
        }
    }

    /**
     * 记录一条操作日志
     *
     * @param opLog 操作日志对象
     */
    void log(OperationLogDTO opLog);

    /**
     * 记录多条操作日志
     *
     * @param opLogList 操作日志对象集合
     */
    void log(@Nonnull Collection<? extends OperationLogDTO> opLogList);

    /**
     * 组装并记录多条操作日志
     *
     * @param opLog        记录模板
     * @param operableList 被操作对象集合
     */
    void log(@Nonnull OperationLogDTO opLog, List<? extends Operable> operableList);

    /**
     * 注册拦截器
     *
     * @param logInterceptor 拦截器
     */
    void addInterceptor(OperationLoggerInterceptor logInterceptor);
}
