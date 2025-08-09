package org.shoulder.monitor.concurrent;

import jakarta.annotation.Nonnull;
import org.shoulder.core.concurrent.enhance.EnhancedCallable;
import org.shoulder.core.concurrent.enhance.EnhancedRunnable;
import org.shoulder.core.concurrent.enhance.ThreadEnhancer;

/**
 * 自动统计在线程池队列内等待时间
 * @deprecated todo 规划后续实现中，暂不可用
 */
public class MonitorRunnableEnhancer implements ThreadEnhancer {

    /**
     * 包装 runnable
     *
     * @param runnable 包装前
     * @return 包装后
     */
    @Nonnull
    @Override
    public EnhancedRunnable doEnhance(@Nonnull EnhancedRunnable runnable) {
        return new DefaultMonitorableRunnable(runnable);
    }

    /**
     * 包装 callable
     *
     * @param callable 包装前
     * @param <T>      泛型
     * @return 包装后
     */
    @Override
    @Nonnull
    public <T> EnhancedCallable<T> doEnhance(@Nonnull EnhancedCallable<T> callable) {
        return new DefaultMonitorableCallable<>(callable);
    }

}
