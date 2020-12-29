package org.shoulder.core.concurrent.enhance;

import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * 包装 AsyncListenableTaskExecutor 如常用的 ThreadPoolTaskExecutor
 *
 * @author lym
 */
public class EnhanceableAsyncListenableTaskExecutor implements AsyncListenableTaskExecutor {

    private final AsyncListenableTaskExecutor delegate;

    public EnhanceableAsyncListenableTaskExecutor(AsyncListenableTaskExecutor delegate) {
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

    @Nonnull
    @Override
    public ListenableFuture<?> submitListenable(@Nonnull Runnable task) {
        return this.delegate.submitListenable(ThreadEnhanceHelper.doEnhance(task));
    }

    @Nonnull
    @Override
    public <T> ListenableFuture<T> submitListenable(@Nonnull Callable<T> task) {
        return this.delegate.submitListenable(ThreadEnhanceHelper.doEnhance(task));
    }

}


