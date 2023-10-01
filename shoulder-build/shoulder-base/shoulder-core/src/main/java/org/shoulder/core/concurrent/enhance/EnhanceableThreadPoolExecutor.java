package org.shoulder.core.concurrent.enhance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;

/**
 * 包装 ThreadPoolExecutor
 *
 * @author lym
 */
public class EnhanceableThreadPoolExecutor extends ThreadPoolExecutor {

    private final ThreadPoolExecutor delegate;

    public EnhanceableThreadPoolExecutor(ThreadPoolExecutor delegate) {
        // 由于继承必须初始化,这里初始化一个0线程的
        super(0, 1, 1, TimeUnit.NANOSECONDS, new LinkedBlockingQueue<>());
        this.delegate = delegate;
    }

    // ====================== ExecutorService ================================

    @Override
    public void shutdown() {
        super.shutdown();
        this.delegate.shutdown();
    }

    @Nonnull
    @Override
    public List<Runnable> shutdownNow() {
        super.shutdownNow();
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
    public <T> List<Future<T>> invokeAll(@Nonnull Collection<? extends Callable<T>> tasks, long timeout, @Nonnull TimeUnit unit)
        throws InterruptedException {
        return this.delegate.invokeAll(this.wrapCallableCollection(tasks), timeout, unit);
    }

    @Nonnull
    @Override
    public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return this.delegate.invokeAny(this.wrapCallableCollection(tasks));
    }

    @Override
    public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> tasks, long timeout, @Nonnull TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        return this.delegate.invokeAny(this.wrapCallableCollection(tasks), timeout, unit);
    }

    @Override
    public void execute(@Nonnull Runnable command) {
        this.delegate.execute(ThreadEnhanceHelper.doEnhance(command));
    }

    // ====================== ThreadPoolExecutor ================================

    public boolean isTerminating() {
        return delegate.isTerminating();
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        delegate.setThreadFactory(threadFactory);
    }

    public ThreadFactory getThreadFactory() {
        return delegate.getThreadFactory();
    }

    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        delegate.setRejectedExecutionHandler(handler);
    }

    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return delegate.getRejectedExecutionHandler();
    }

    public int getCorePoolSize() {
        return delegate.getCorePoolSize();
    }

    public boolean prestartCoreThread() {
        return delegate.prestartCoreThread();
    }

    public int prestartAllCoreThreads() {
        return delegate.prestartAllCoreThreads();
    }

    public boolean allowsCoreThreadTimeOut() {
        return delegate.allowsCoreThreadTimeOut();
    }

    public void allowCoreThreadTimeOut(boolean value) {
        delegate.allowCoreThreadTimeOut(value);
    }

    public int getMaximumPoolSize() {
        return delegate.getMaximumPoolSize();
    }

    public void setKeepAliveTime(long time, TimeUnit unit) {
        delegate.setKeepAliveTime(time, unit);
    }

    public long getKeepAliveTime(TimeUnit unit) {
        return delegate.getKeepAliveTime(unit);
    }

    public BlockingQueue<Runnable> getQueue() {
        return delegate.getQueue();
    }

    public boolean remove(Runnable task) {
        // todo 需要注意如何 remove,或者提供不自动包装的配置
        throw new UnsupportedOperationException("unSupportRemove");
        //return delegate.remove(task);
    }

    public void purge() {
        delegate.purge();
    }

    public int getPoolSize() {
        return delegate.getPoolSize();
    }

    public int getActiveCount() {
        return delegate.getActiveCount();
    }

    public int getLargestPoolSize() {
        return delegate.getLargestPoolSize();
    }

    public long getTaskCount() {
        return delegate.getTaskCount();
    }

    public long getCompletedTaskCount() {
        return delegate.getCompletedTaskCount();
    }

    public String toString() {
        return delegate.toString();
    }

}
