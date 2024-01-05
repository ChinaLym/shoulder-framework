package org.shoulder.log.operation.context;

import jakarta.annotation.Nonnull;

/**
 * shoulder 定义的业务，传播
 *
 * @see OperationContextStrategyEnum 默认实现了几种
 * @author lym
 */
public interface OperationContextStrategy {

    /**
     * 创建一个新的上下文，与之前无任何关系【一般场景，最常用的】
     */
    int CREATE_NEW = 0;

    /**
     * 忽略本次【个别场景，少量使用】
     */
    int IGNORE = 1;

    /**
     * 认为不应该存在嵌套调用，抛异常
     */
    int THROW_EX = 2;

    /**
     * 复制已有的上下文，（暂未遇到适用的业务场景）
     */
    int COPY = 3;

    /**
     * 不做任何操作，仅表示该方法内存在从操作日志上下文中获取日志并填充的代码，即暗示调用时存在操作日志上下文（相当于没加注解，一般也不会用）
     */
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
