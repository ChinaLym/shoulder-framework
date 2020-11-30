package org.shoulder.log.operation;

import org.shoulder.log.operation.util.OpLogContext;

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
     * @return 返回的日志上下文，若返回 null，即不创建，忽略该上下文中所有日志工具相关方法
     */
    OpLogContext onMissingContext();


    /**
     * 当前线程有操作日志上下文信息时策略
     *
     * @return 返回的日志上下文，若返回 null，即不创建，忽略该上下文中所有日志工具相关方法
     */
    OpLogContext onExistContext(OpLogContext existed);

}
