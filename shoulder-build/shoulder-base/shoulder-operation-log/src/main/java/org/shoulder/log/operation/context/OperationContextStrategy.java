package org.shoulder.log.operation.context;

import javax.annotation.Nonnull;

/**
 * shoulder 定义的业务，传播
 *
 * @author lym
 */
public interface OperationContextStrategy {

    int CREATE_NEW = 0;
    int IGNORE = 1;
    int THROW_EX = 2;
    int COPY = 3;
    int REUSE = 4;

    /**
     * 当前线程没有操作日志上下文信息时策略
     *
     * @return 返回的日志上下文
     */
    @Nonnull
    OpLogContext onMissingContext();


    /**
     * 当前线程有操作日志上下文信息时策略
     *
     * @param existed 当前线程中的日志上下文
     * @return 返回的日志上下文
     */
    @Nonnull
    OpLogContext onExistContext(OpLogContext existed);

}
