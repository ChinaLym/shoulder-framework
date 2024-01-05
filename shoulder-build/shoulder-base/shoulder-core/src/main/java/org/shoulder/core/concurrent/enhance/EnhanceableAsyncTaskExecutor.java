package org.shoulder.core.concurrent.enhance;

import jakarta.annotation.Nonnull;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * 包装 AsyncTaskExecutor
 *
 * @author lym
 */
public class EnhanceableAsyncTaskExecutor implements AsyncTaskExecutor {

    private final AsyncTaskExecutor delegate;

    public EnhanceableAsyncTaskExecutor(AsyncTaskExecutor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(@Nonnull Runnable task) {
        this.delegate.execute(ThreadEnhanceHelper.doEnhance(task));
    }

    @Override
    public void execute(@Nonnull Runnable task, long startTimeout) {
        this.delegate.execute((ThreadEnhanceHelper.doEnhance(task)), startTimeout);
    }

    @Nonnull
    @Override
    public Future<?> submit(@Nonnull Runnable task) {
        return this.delegate.submit(ThreadEnhanceHelper.doEnhance(task));
    }

    @Nonnull
    @Override
    public <T> Future<T> submit(@Nonnull Callable<T> task) {
        return this.delegate.submit(ThreadEnhanceHelper.doEnhance(task));
    }

}


