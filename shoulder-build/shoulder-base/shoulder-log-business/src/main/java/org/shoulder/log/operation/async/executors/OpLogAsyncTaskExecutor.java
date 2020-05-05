package org.shoulder.log.operation.async.executors;

import org.shoulder.log.operation.async.OpLogCallable;
import org.shoulder.log.operation.async.OpLogRunnable;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.lang.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * 包装 AsyncTaskExecutor
 * @author lym
 */
public class OpLogAsyncTaskExecutor implements AsyncTaskExecutor {
    private final AsyncTaskExecutor delegate;

    public OpLogAsyncTaskExecutor(AsyncTaskExecutor delegate) {
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

}


