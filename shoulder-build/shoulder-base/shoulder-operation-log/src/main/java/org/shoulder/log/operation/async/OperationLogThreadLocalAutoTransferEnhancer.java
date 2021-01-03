package org.shoulder.log.operation.async;

import org.shoulder.core.concurrent.enhance.EnhancedCallable;
import org.shoulder.core.concurrent.enhance.EnhancedRunnable;
import org.shoulder.core.concurrent.enhance.ThreadEnhancer;

/**
 * 自动将线程变量转移
 *
 * @author lym
 */
public class OperationLogThreadLocalAutoTransferEnhancer implements ThreadEnhancer {

    /**
     * 包装 runnable
     *
     * @param runnable 包装前
     * @return 包装后
     */
    @Override
    public EnhancedRunnable doEnhance(EnhancedRunnable runnable) {
        return new EnhancedRunnable(new OpLogRunnable(runnable));
    }

    /**
     * 包装 callable
     *
     * @param callable 包装前
     * @param <T>      泛型
     * @return 包装后
     */
    @Override
    public <T> EnhancedCallable<T> doEnhance(EnhancedCallable<T> callable) {
        return new EnhancedCallable<>(new OpLogCallable<>(callable));
    }

}
