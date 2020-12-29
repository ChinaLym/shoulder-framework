package org.shoulder.core.concurrent.enhance;

import java.util.concurrent.Callable;

/**
 * 线程增强（装饰者模式-装饰器接口）
 *
 * @author lym
 */
public interface ThreadEnhancer {

    /**
     * 包装 runnable 【默认什么也不做】
     *
     * @param runnable 包装前
     * @return 包装后
     */
    default Runnable doEnhance(Runnable runnable) {
        return runnable;
    }

    /**
     * 包装 callable 【默认什么也不做】
     *
     * @param callable 包装前
     * @param <T>      泛型
     * @return 包装后
     */
    default <T> Callable<T> doEnhance(Callable<T> callable) {
        return callable;
    }

}