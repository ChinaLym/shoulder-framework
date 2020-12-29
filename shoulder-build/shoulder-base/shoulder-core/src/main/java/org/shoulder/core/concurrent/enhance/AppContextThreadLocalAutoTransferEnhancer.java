package org.shoulder.core.concurrent.enhance;

import org.shoulder.core.context.AppContext;

import java.util.Map;
import java.util.concurrent.Callable;

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
    public Runnable doEnhance(Runnable runnable) {
        Map<String, Object> temp = AppContext.getAll();
        return () -> {
            try {
                AppContext.set(temp);
                runnable.run();
            } finally {
                AppContext.clean();
            }
        };
    }

    /**
     * 包装 callable
     *
     * @param callable 包装前
     * @param <T>      泛型
     * @return 包装后
     */
    @Override
    public <T> Callable<T> doEnhance(Callable<T> callable) {
        Map<String, Object> temp = AppContext.getAll();
        return () -> {
            try {
                AppContext.set(temp);
                return callable.call();
            } finally {
                AppContext.clean();
            }
        };
    }

}
