package org.shoulder.core.concurrent.enhance;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 包装 JDK.ScheduledThreadPoolExecutor JDK 提供的任务调度执行器
 *
 * @author lym
 */
public class EnhanceableScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor implements EnhanceableExecutorMark {

    private static final Map<ScheduledThreadPoolExecutor, EnhanceableScheduledThreadPoolExecutor> CACHE = new ConcurrentHashMap<>();

    private final ScheduledThreadPoolExecutor delegate;

    private final Method decorateTaskRunnable;

    private final Method decorateTaskCallable;

    private final Method beforeExecute;

    private final Method afterExecute;

    private final Method terminated;

    private final Method newTaskForRunnable;

    private final Method newTaskForCallable;

    EnhanceableScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory,
                                           RejectedExecutionHandler handler, ScheduledThreadPoolExecutor delegate) {
        super(corePoolSize, threadFactory, handler);
        this.delegate = delegate;
        Method decorateTaskRunnable = ReflectionUtils.findMethod(ScheduledThreadPoolExecutor.class, "decorateTask",
                Runnable.class, RunnableScheduledFuture.class);
        this.decorateTaskRunnable = makeAccessibleIfNotNullAndOverridden(decorateTaskRunnable);
        Method decorateTaskCallable = ReflectionUtils.findMethod(ScheduledThreadPoolExecutor.class, "decorateTask",
                Callable.class, RunnableScheduledFuture.class);
        this.decorateTaskCallable = makeAccessibleIfNotNullAndOverridden(decorateTaskCallable);
        Method beforeExecute = ReflectionUtils.findMethod(ScheduledThreadPoolExecutor.class, "beforeExecute", null);
        this.beforeExecute = makeAccessibleIfNotNullAndOverridden(beforeExecute);
        Method afterExecute = ReflectionUtils.findMethod(ScheduledThreadPoolExecutor.class, "afterExecute", null);
        this.afterExecute = makeAccessibleIfNotNullAndOverridden(afterExecute);
        Method terminated = ReflectionUtils.findMethod(ScheduledThreadPoolExecutor.class, "terminated", null);
        this.terminated = makeAccessibleIfNotNullAndOverridden(terminated);
        Method newTaskForRunnable = ReflectionUtils.findMethod(ScheduledThreadPoolExecutor.class, "newTaskFor",
                Runnable.class, Object.class);
        this.newTaskForRunnable = makeAccessibleIfNotNullAndOverridden(newTaskForRunnable);
        Method newTaskForCallable = ReflectionUtils.findMethod(ScheduledThreadPoolExecutor.class, "newTaskFor",
                Callable.class);
        this.newTaskForCallable = makeAccessibleIfNotNullAndOverridden(newTaskForCallable);
    }

    public static EnhanceableScheduledThreadPoolExecutor wrap(int corePoolSize, ThreadFactory threadFactory,
                                                       RejectedExecutionHandler handler, @NonNull ScheduledThreadPoolExecutor delegate) {
        return CACHE.computeIfAbsent(delegate, e -> new EnhanceableScheduledThreadPoolExecutor(corePoolSize,
                threadFactory, handler, delegate));
    }
    
    @Nullable
    private Method makeAccessibleIfNotNullAndOverridden(@Nullable Method method) {
        if (method != null) {
            if (isMethodOverridden(method)) {
                try {
                    ReflectionUtils.makeAccessible(method);
                    return method;
                }
                catch (Throwable ex) {
                    if (anyCauseIsInaccessibleObjectException(ex)) {
                        throw new IllegalStateException("The executor [" + this.delegate.getClass()
                                + "] has overridden a method with name [" + method.getName()
                                + "] and the object is inaccessible. You have to run your JVM with [--add-opens] switch to allow such access. Example: [--add-opens java.base/java.util.concurrent=ALL-UNNAMED].",
                                ex);
                    }
                    throw ex;
                }
            }
        }
        return null;
    }

    private boolean anyCauseIsInaccessibleObjectException(Throwable t) {
        Throwable parent = t;
        Throwable cause = t.getCause();
        while (cause != null && cause != parent) {
            if (cause.getClass().toString().contains("InaccessibleObjectException")) {
                return true;
            }
            parent = cause;
            cause = parent.getCause();
        }
        return false;
    }

    boolean isMethodOverridden(Method originalMethod) {
        Method delegateMethod = ReflectionUtils.findMethod(this.delegate.getClass(), originalMethod.getName());
        if (delegateMethod == null) {
            return false;
        }
        return !delegateMethod.equals(originalMethod);
    }

    /// ------------------------ Override -----------------------------

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
        if (this.decorateTaskRunnable == null) {
            return super.decorateTask(ThreadEnhanceHelper.doEnhance(runnable), task);
        }
        return (RunnableScheduledFuture<V>) ReflectionUtils.invokeMethod(this.decorateTaskRunnable, this.delegate,
                ThreadEnhanceHelper.doEnhance(runnable), task);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task) {
        if (this.decorateTaskCallable == null) {
            return super.decorateTask(ThreadEnhanceHelper.doEnhance(callable), task);
        }
        return (RunnableScheduledFuture<V>) ReflectionUtils.invokeMethod(this.decorateTaskCallable, this.delegate,
                ThreadEnhanceHelper.doEnhance(callable), task);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return this.delegate.schedule(ThreadEnhanceHelper.doEnhance(command), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return this.delegate.schedule(ThreadEnhanceHelper.doEnhance(callable), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return this.delegate.scheduleAtFixedRate(ThreadEnhanceHelper.doEnhance(command), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return this.delegate.scheduleWithFixedDelay(ThreadEnhanceHelper.doEnhance(command), initialDelay, delay, unit);
    }

    @Override
    public void execute(Runnable command) {
        this.delegate.execute(ThreadEnhanceHelper.doEnhance(command));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return this.delegate.submit(ThreadEnhanceHelper.doEnhance(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return this.delegate.submit(ThreadEnhanceHelper.doEnhance(task), result);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return this.delegate.submit(ThreadEnhanceHelper.doEnhance(task));
    }

    @Override
    public void setContinueExistingPeriodicTasksAfterShutdownPolicy(boolean value) {
        this.delegate.setContinueExistingPeriodicTasksAfterShutdownPolicy(value);
    }

    @Override
    public boolean getContinueExistingPeriodicTasksAfterShutdownPolicy() {
        return this.delegate.getContinueExistingPeriodicTasksAfterShutdownPolicy();
    }

    @Override
    public void setExecuteExistingDelayedTasksAfterShutdownPolicy(boolean value) {
        this.delegate.setExecuteExistingDelayedTasksAfterShutdownPolicy(value);
    }

    @Override
    public boolean getExecuteExistingDelayedTasksAfterShutdownPolicy() {
        return this.delegate.getExecuteExistingDelayedTasksAfterShutdownPolicy();
    }

    @Override
    public void setRemoveOnCancelPolicy(boolean value) {
        this.delegate.setRemoveOnCancelPolicy(value);
    }

    @Override
    public boolean getRemoveOnCancelPolicy() {
        return this.delegate.getRemoveOnCancelPolicy();
    }

    @Override
    public void shutdown() {
        this.delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return this.delegate.shutdownNow();
    }

    @Override
    public BlockingQueue<Runnable> getQueue() {
        return this.delegate.getQueue();
    }

    @Override
    public boolean isShutdown() {
        return this.delegate.isShutdown();
    }

    @Override
    public boolean isTerminating() {
        return this.delegate.isTerminating();
    }

    @Override
    public boolean isTerminated() {
        return this.delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return this.delegate.awaitTermination(timeout, unit);
    }

    @Override
    public void setThreadFactory(ThreadFactory threadFactory) {
        this.delegate.setThreadFactory(threadFactory);
    }

    @Override
    public ThreadFactory getThreadFactory() {
        return this.delegate.getThreadFactory();
    }

    @Override
    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        this.delegate.setRejectedExecutionHandler(handler);
    }

    @Override
    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return this.delegate.getRejectedExecutionHandler();
    }

    @Override
    public void setCorePoolSize(int corePoolSize) {
        this.delegate.setCorePoolSize(corePoolSize);
    }

    @Override
    public int getCorePoolSize() {
        return this.delegate.getCorePoolSize();
    }

    @Override
    public boolean prestartCoreThread() {
        return this.delegate.prestartCoreThread();
    }

    @Override
    public int prestartAllCoreThreads() {
        return this.delegate.prestartAllCoreThreads();
    }

    @Override
    public boolean allowsCoreThreadTimeOut() {
        return this.delegate.allowsCoreThreadTimeOut();
    }

    @Override
    public void allowCoreThreadTimeOut(boolean value) {
        this.delegate.allowCoreThreadTimeOut(value);
    }

    @Override
    public void setMaximumPoolSize(int maximumPoolSize) {
        this.delegate.setMaximumPoolSize(maximumPoolSize);
    }

    @Override
    public int getMaximumPoolSize() {
        return this.delegate.getMaximumPoolSize();
    }

    @Override
    public void setKeepAliveTime(long time, TimeUnit unit) {
        this.delegate.setKeepAliveTime(time, unit);
    }

    @Override
    public long getKeepAliveTime(TimeUnit unit) {
        return this.delegate.getKeepAliveTime(unit);
    }

    @Override
    public boolean remove(Runnable task) {
        return this.delegate.remove(ThreadEnhanceHelper.doEnhance(task));
    }

    @Override
    public void purge() {
        this.delegate.purge();
    }

    @Override
    public int getPoolSize() {
        return this.delegate.getPoolSize();
    }

    @Override
    public int getActiveCount() {
        return this.delegate.getActiveCount();
    }

    @Override
    public int getLargestPoolSize() {
        return this.delegate.getLargestPoolSize();
    }

    @Override
    public long getTaskCount() {
        return this.delegate.getTaskCount();
    }

    @Override
    public long getCompletedTaskCount() {
        return this.delegate.getCompletedTaskCount();
    }

    @Override
    public String toString() {
        return this.delegate.toString();
    }

    @Override
    public void beforeExecute(Thread t, Runnable r) {
        if (this.beforeExecute == null) {
            super.beforeExecute(t, ThreadEnhanceHelper.doEnhance(r));
            return;
        }
        ReflectionUtils.invokeMethod(this.beforeExecute, this.delegate, t, ThreadEnhanceHelper.doEnhance(r));
    }

    @Override
    public void afterExecute(Runnable r, Throwable t) {
        if (this.afterExecute == null) {
            super.afterExecute(ThreadEnhanceHelper.doEnhance(r), t);
            return;
        }
        ReflectionUtils.invokeMethod(this.afterExecute, this.delegate, ThreadEnhanceHelper.doEnhance(r), t);
    }

    @Override
    public void terminated() {
        if (this.terminated == null) {
            super.terminated();
            return;
        }
        ReflectionUtils.invokeMethod(this.terminated, this.delegate);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        if (this.newTaskForRunnable == null) {
            return super.newTaskFor(ThreadEnhanceHelper.doEnhance(runnable), value);
        }
        return (RunnableFuture<T>) ReflectionUtils.invokeMethod(this.newTaskForRunnable, this.delegate,
                ThreadEnhanceHelper.doEnhance(runnable), value);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        if (this.newTaskForRunnable == null) {
            return super.newTaskFor(ThreadEnhanceHelper.doEnhance(callable));
        }
        return (RunnableFuture<T>) ReflectionUtils.invokeMethod(this.newTaskForCallable, this.delegate,
                ThreadEnhanceHelper.doEnhance(callable));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return this.delegate.invokeAny(ThreadEnhanceHelper.doEnhance(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return this.delegate.invokeAny(ThreadEnhanceHelper.doEnhance(tasks), timeout, unit);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return this.delegate.invokeAll(ThreadEnhanceHelper.doEnhance(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        return this.delegate.invokeAll(ThreadEnhanceHelper.doEnhance(tasks), timeout, unit);
    }

}


