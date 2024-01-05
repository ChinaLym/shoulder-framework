package org.shoulder.core.concurrent.enhance;

import org.shoulder.core.context.AppContext;

import java.io.Serializable;
import java.util.Map;

/**
 * 自动将线程变量转移
 *
 * @author lym
 */
public class AppContextThreadLocalAutoTransferEnhancer implements ThreadEnhancer {

    /**
     * 包装 runnable
     *
     * @param runnable 包装前
     * @return 包装后
     */
    @Override
    public EnhancedRunnable doEnhance(EnhancedRunnable runnable) {
        Map<String, Serializable> temp = AppContext.getAll();
        return new EnhancedRunnable(() -> {
            try {
                AppContext.set(temp);
                runnable.run();
            } finally {
                AppContext.clean();
            }
        });
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
        Map<String, Serializable> temp = AppContext.getAll();
        return new EnhancedCallable<>(() -> {
            try {
                AppContext.set(temp);
                return callable.call();
            } finally {
                AppContext.clean();
            }
        });
    }

}
