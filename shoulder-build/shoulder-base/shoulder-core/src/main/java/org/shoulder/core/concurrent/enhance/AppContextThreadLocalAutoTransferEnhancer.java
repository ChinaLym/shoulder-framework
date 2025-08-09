package org.shoulder.core.concurrent.enhance;

import jakarta.annotation.Nonnull;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.util.TraceIdGenerator;

import java.io.Serializable;
import java.util.Map;

/**
 * 自动将线程变量转移
 *
 * @author lym
 */
public class AppContextThreadLocalAutoTransferEnhancer implements ThreadEnhancer {

    /**
     * Wrap runnable
     *
     * @param runnable task
     * @return enhancedRunnable
     */
    @Override
    public EnhancedRunnable doEnhance(@Nonnull EnhancedRunnable runnable) {
        Map<String, Serializable> allContext = AppContext.getAll();
        return new EnhancedRunnable(() -> {
            try {
                AppContext.set(allContext);
                TraceIdGenerator.checkContextTracOrGenerateNew();
                runnable.run();
            } finally {
                AppContext.clean();
            }
        });
    }

    /**
     * Wrap callable
     *
     * @param callable 包装前
     * @param <T>      泛型
     * @return 包装后
     */
    @Override
    public <T> EnhancedCallable<T> doEnhance(EnhancedCallable<T> callable) {
        Map<String, Serializable> allContext = AppContext.getAll();
        return new EnhancedCallable<>(() -> {
            try {
                AppContext.set(allContext);
                TraceIdGenerator.checkContextTracOrGenerateNew();
                return callable.call();
            } finally {
                AppContext.clean();
            }
        });
    }

}
