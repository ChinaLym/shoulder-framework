package org.shoulder.core.concurrent.enhance;

import jakarta.annotation.Nonnull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * 包装 JDK.i.Executor 最基础最通用的线程执行器接口
 *
 * @author lym
 */
public class EnhanceableExecutor implements Executor, EnhanceableExecutorMark {
    
    private static final Map<Executor, EnhanceableExecutor> CACHE = new ConcurrentHashMap<>();
    
    private final Executor delegate;

    public EnhanceableExecutor(Executor delegate) {
        this.delegate = delegate;
    }

    /**
     * Wraps the Executor in a trace instance.
     *
     * @param delegate delegate to wrap
     */
    public static EnhanceableExecutor wrap(Executor delegate) {
        return CACHE.computeIfAbsent(delegate, e -> new EnhanceableExecutor(delegate));
    }
    
    @Override
    public void execute(@Nonnull Runnable command) {
        this.delegate.execute(ThreadEnhanceHelper.doEnhance(command));
    }
}
