package org.shoulder.core.concurrent.enhance;

import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 包装 JKD.i.ExecutorService 线程池通用接口
 *
 * @author lym
 */
public class EnhanceableExecutorService implements ExecutorService, EnhanceableExecutorMark {

    private static final Map<ExecutorService, EnhanceableExecutorService> CACHE = new ConcurrentHashMap<>();

    protected final ExecutorService delegate;

    public EnhanceableExecutorService(ExecutorService delegate) {
        this.delegate = delegate;
    }

    public static ExecutorService wrap(ExecutorService delegate) {
        return CACHE.computeIfAbsent(delegate,
                e -> new EnhanceableExecutorService(delegate));
    }

    @Override
    public void shutdown() {
        this.delegate.shutdown();
    }

    @Nonnull
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
    public boolean awaitTermination(long timeout, @Nonnull TimeUnit unit) throws InterruptedException {
        return this.delegate.awaitTermination(timeout, unit);
    }

    @Nonnull
    @Override
    public <T> Future<T> submit(@Nonnull Callable<T> task) {
        return this.delegate.submit(ThreadEnhanceHelper.doEnhance(task));
    }

    @Nonnull
    @Override
    public <T> Future<T> submit(@Nonnull Runnable task, T result) {
        return this.delegate.submit(ThreadEnhanceHelper.doEnhance(task), result);
    }

    @Nonnull
    @Override
    public Future<?> submit(@Nonnull Runnable task) {
        return this.delegate.submit(ThreadEnhanceHelper.doEnhance(task));
    }

    @Nonnull
    @Override
    public <T> List<Future<T>> invokeAll(@Nonnull Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return this.delegate.invokeAll(this.wrapCallableCollection(tasks));
    }

    private <T> Collection<? extends Callable<T>> wrapCallableCollection(Collection<? extends Callable<T>> tasks) {
        List<Callable<T>> ts = new ArrayList<>(tasks.size());
        for (Callable<T> task : tasks) {
            ts.add(ThreadEnhanceHelper.doEnhance(task));
        }
        return ts;
    }

    @Nonnull
    @Override
    public <T> List<Future<T>> invokeAll(@Nonnull Collection<? extends Callable<T>> tasks, long timeout, @Nonnull TimeUnit unit) throws InterruptedException {
        return this.delegate.invokeAll(this.wrapCallableCollection(tasks), timeout, unit);
    }

    @Nonnull
    @Override
    public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return this.delegate.invokeAny(this.wrapCallableCollection(tasks));
    }


    @Override
    public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> tasks, long timeout, @Nonnull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.delegate.invokeAny(this.wrapCallableCollection(tasks), timeout, unit);
    }

    @Override
    public void execute(@Nonnull Runnable command) {
        this.delegate.execute(ThreadEnhanceHelper.doEnhance(command));
    }
}
