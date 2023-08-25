package org.shoulder.monitor.concurrent;

import org.shoulder.core.concurrent.enhance.EnhancedCallable;
import org.shoulder.core.concurrent.enhance.EnhancedRunnable;
import org.shoulder.core.concurrent.enhance.ThreadEnhancer;

/**
 * 自动统计在线程池队列内等待时间
 */
public class MonitorRunnableEnhancer implements ThreadEnhancer {

    /**
     * 包装 runnable
     *
     * @param runnable 包装前
     * @return 包装后
     */
    @Override
    public EnhancedRunnable doEnhance(EnhancedRunnable runnable) {
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
    public <T> EnhancedCallable<T> doEnhance(EnhancedCallable<T> callable) {
        return new DefaultMonitorableCallable<>(callable);
    }

}
