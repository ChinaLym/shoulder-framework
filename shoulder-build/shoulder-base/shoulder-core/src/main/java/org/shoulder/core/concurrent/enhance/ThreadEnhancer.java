package org.shoulder.core.concurrent.enhance;

import java.util.concurrent.Callable;

/**
 * 线程增强
 *
 * @author lym
 */
public interface ThreadEnhancer {

    /**
     * 包装 runnable
     *
     * @param runnable 包装前
     * @return 包装后
     */
    default Runnable doEnhance(Runnable runnable) {
        return runnable;
    }

    /**
     * 包装 callable
     *
     * @param callable 包装前
     * @param <T>      泛型
     * @return 包装后
     */
    default <T> Callable<T> doEnhance(Callable<T> callable) {
        return callable;
    }

}
