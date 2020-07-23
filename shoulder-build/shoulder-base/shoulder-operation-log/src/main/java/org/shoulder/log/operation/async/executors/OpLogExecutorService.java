package org.shoulder.log.operation.async.executors;

import org.shoulder.log.operation.async.OpLogCallable;
import org.shoulder.log.operation.async.OpLogRunnable;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * 包装 ExecutorService
 * 如常用的 ThreadPoolExecutor
 * @author lym
 */
public class OpLogExecutorService implements ExecutorService {

    private final ExecutorService delegate;

    public OpLogExecutorService(ExecutorService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void shutdown() {
        this.delegate.shutdown();
    }

    @NonNull
    @Override
    public List<Runnable> shutdownNow() {
        return this.delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return this.delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return this.delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, @NonNull  TimeUnit unit) throws InterruptedException {
        return this.delegate.awaitTermination(timeout, unit);
    }

    @NonNull
    @Override
    public <T> Future<T> submit(@NonNull  Callable<T> task) {
        return this.delegate.submit(new OpLogCallable<>(task));
    }

    @NonNull
    @Override
    public <T> Future<T> submit(@NonNull  Runnable task, T result) {
        return this.delegate.submit(new OpLogRunnable(task), result);
    }

    @NonNull
    @Override
    public Future<?> submit(@NonNull  Runnable task) {
        return this.delegate.submit(new OpLogRunnable(task));
    }

    @NonNull
    @Override
    public <T> List<Future<T>> invokeAll(@NonNull  Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return this.delegate.invokeAll(this.wrapCallableCollection(tasks));
    }

    private <T> Collection<? extends Callable<T>> wrapCallableCollection(Collection<? extends Callable<T>> tasks) {
        List<Callable<T>> ts = new ArrayList<>(tasks.size());
        for (Callable<T> task : tasks) {
            if (!(task instanceof OpLogCallable)) {
                ts.add(new OpLogCallable<>(task));
            }
        }
        return ts;
    }

    @NonNull
    @Override
    public <T> List<Future<T>> invokeAll(@NonNull  Collection<? extends Callable<T>> tasks, long timeout, @NonNull  TimeUnit unit) throws InterruptedException {
        return this.delegate.invokeAll(this.wrapCallableCollection(tasks), timeout, unit);
    }

    @NonNull
    @Override
    public <T> T invokeAny(@NonNull  Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return this.delegate.invokeAny(this.wrapCallableCollection(tasks));
    }

    
    @Override
    public <T> T invokeAny(@NonNull  Collection<? extends Callable<T>> tasks, long timeout, @NonNull  TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.delegate.invokeAny(this.wrapCallableCollection(tasks), timeout, unit);
    }

    @Override
    public void execute(@NonNull  Runnable command) {
        this.delegate.execute(new OpLogRunnable(command));
    }
}
