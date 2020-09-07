package org.shoulder.log.operation.async.executors;

import org.shoulder.log.operation.async.OpLogCallable;
import org.shoulder.log.operation.async.OpLogRunnable;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.lang.NonNull;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * 包装 AsyncListenableTaskExecutor 如常用的 ThreadPoolTaskExecutor
 *
 * @author lym
 */
public class OpLogAsyncListenableTaskExecutor implements AsyncListenableTaskExecutor {

    private final AsyncListenableTaskExecutor delegate;

    public OpLogAsyncListenableTaskExecutor(AsyncListenableTaskExecutor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(@NonNull Runnable task) {
        this.delegate.execute(new OpLogRunnable(task));
    }

    @Override
    public void execute(@NonNull Runnable task, long startTimeout) {
        this.delegate.execute((new OpLogRunnable(task)), startTimeout);
    }

    @NonNull
    @Override
    public Future<?> submit(@NonNull Runnable task) {
        return this.delegate.submit(new OpLogRunnable(task));
    }

    @NonNull
    @Override
    public <T> Future<T> submit(@NonNull Callable<T> task) {
        return this.delegate.submit(new OpLogCallable<>(task));
    }

    @NonNull
    @Override
    public ListenableFuture<?> submitListenable(@NonNull Runnable task) {
        return this.delegate.submitListenable(new OpLogRunnable(task));
    }

    @NonNull
    @Override
    public <T> ListenableFuture<T> submitListenable(@NonNull Callable<T> task) {
        return this.delegate.submitListenable(new OpLogCallable<>(task));
    }

}


