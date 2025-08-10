package org.shoulder.core.concurrent.enhance;

import jakarta.annotation.Nonnull;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * 包装 Spring.i.AsyncTaskExecutor （类比 JDK.Executor）异步任务执行器接口
 * 要用 AsyncTaskExecutor 而不用 AsyncListenableTaskExecutor（已废弃）
 *
 * @author lym
 */
public class EnhanceableAsyncTaskExecutor implements AsyncTaskExecutor, EnhanceableExecutorMark {

    private static final Map<AsyncTaskExecutor, EnhanceableAsyncTaskExecutor> CACHE = new ConcurrentHashMap<>();

    private final AsyncTaskExecutor delegate;

    public EnhanceableAsyncTaskExecutor(AsyncTaskExecutor delegate) {
        this.delegate = delegate;
    }

    public static EnhanceableAsyncTaskExecutor wrap(AsyncTaskExecutor delegate) {
        return CACHE.computeIfAbsent(delegate,
                e -> new EnhanceableAsyncTaskExecutor(delegate));
    }

    @Override
    public void execute(Runnable task) {
        this.delegate.execute(ThreadEnhanceHelper.doEnhance(task));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(Runnable task, long startTimeout) {
        this.delegate.execute((ThreadEnhanceHelper.doEnhance(task)), startTimeout);
    }

    @Nonnull
    @Override
    public Future<?> submit(Runnable task) {
        return this.delegate.submit(ThreadEnhanceHelper.doEnhance(task));
    }

    @Nonnull
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return this.delegate.submit(ThreadEnhanceHelper.doEnhance(task));
    }

}


