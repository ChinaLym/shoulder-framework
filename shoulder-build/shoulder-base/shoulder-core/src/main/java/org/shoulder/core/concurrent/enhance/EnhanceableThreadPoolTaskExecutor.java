package org.shoulder.core.concurrent.enhance;

import jakarta.annotation.Nullable;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Map;
import java.util.concurrent.*;

/**
 * JDK 并发线程池
 *
 * @author lym
 */
public class EnhanceableThreadPoolTaskExecutor extends ThreadPoolTaskExecutor implements EnhanceableExecutorMark {

    private static final Map<ThreadPoolTaskExecutor, EnhanceableThreadPoolTaskExecutor> CACHE = new ConcurrentHashMap<>();

    private final ThreadPoolTaskExecutor delegate;

    public EnhanceableThreadPoolTaskExecutor(ThreadPoolTaskExecutor delegate) {
        this.delegate = delegate;
    }

    /**
     * Wraps the Executor in a trace instance.
     *
     * @param delegate delegate to wrap
     */
    public static EnhanceableThreadPoolTaskExecutor wrap(ThreadPoolTaskExecutor delegate) {
        return CACHE.computeIfAbsent(delegate,
                e -> new EnhanceableThreadPoolTaskExecutor(delegate));
    }


    @Override
    public void execute(Runnable task) {
        this.delegate.execute(ThreadEnhanceHelper.doEnhance(task));
    }

    @Override
    public void execute(Runnable task, long startTimeout) {
        this.delegate.execute(ThreadEnhanceHelper.doEnhance(task), startTimeout);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return this.delegate.submit(ThreadEnhanceHelper.doEnhance(task));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return this.delegate.submit(ThreadEnhanceHelper.doEnhance(task));
    }

    @Override
    public ListenableFuture<?> submitListenable(@Nullable Runnable task) {
        return this.delegate.submitListenable(ThreadEnhanceHelper.doEnhance(task));
    }

    @Override
    public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
        return this.delegate.submitListenable(ThreadEnhanceHelper.doEnhance(task));
    }

    @Override
    public boolean prefersShortLivedTasks() {
        return this.delegate.prefersShortLivedTasks();
    }

    @Override
    public void setThreadFactory(@Nullable ThreadFactory threadFactory) {
        this.delegate.setThreadFactory(threadFactory);
    }

    @Override
    public void setRejectedExecutionHandler(@Nullable RejectedExecutionHandler rejectedExecutionHandler) {
        this.delegate.setRejectedExecutionHandler(rejectedExecutionHandler);
    }

    @Override
    public void setWaitForTasksToCompleteOnShutdown(boolean waitForJobsToCompleteOnShutdown) {
        this.delegate.setWaitForTasksToCompleteOnShutdown(waitForJobsToCompleteOnShutdown);
    }

    @Override
    public void setAwaitTerminationSeconds(int awaitTerminationSeconds) {
        this.delegate.setAwaitTerminationSeconds(awaitTerminationSeconds);
    }

    @Override
    public void setBeanName(String name) {
        this.delegate.setBeanName(name);
    }

    @Override
    public ThreadPoolExecutor getThreadPoolExecutor() throws IllegalStateException {
        return this.delegate.getThreadPoolExecutor();
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
    public int getQueueCapacity() {
        return this.delegate.getQueueCapacity();
    }

    @Override
    public void destroy() {
        this.delegate.destroy();
        super.destroy();
    }

    @Override
    public void afterPropertiesSet() {
        this.delegate.afterPropertiesSet();
        super.afterPropertiesSet();
    }

    @Override
    public void initialize() {
        this.delegate.initialize();
    }

    @Override
    public void shutdown() {
        this.delegate.shutdown();
        super.shutdown();
    }

    @Override
    public Thread newThread(Runnable runnable) {
        return this.delegate.newThread(ThreadEnhanceHelper.doEnhance(runnable));
    }

    @Override
    public String getThreadNamePrefix() {
        return this.delegate.getThreadNamePrefix();
    }

    @Override
    public void setThreadNamePrefix(@Nullable String threadNamePrefix) {
        this.delegate.setThreadNamePrefix(threadNamePrefix);
    }

    @Override
    public int getThreadPriority() {
        return this.delegate.getThreadPriority();
    }

    @Override
    public void setThreadPriority(int threadPriority) {
        this.delegate.setThreadPriority(threadPriority);
    }

    @Override
    public boolean isDaemon() {
        return this.delegate.isDaemon();
    }

    @Override
    public void setDaemon(boolean daemon) {
        this.delegate.setDaemon(daemon);
    }

    @Override
    public void setThreadGroupName(String name) {
        this.delegate.setThreadGroupName(name);
    }

    @Nullable
    @Override
    public ThreadGroup getThreadGroup() {
        return this.delegate.getThreadGroup();
    }

    @Override
    public void setThreadGroup(@Nullable ThreadGroup threadGroup) {
        this.delegate.setThreadGroup(threadGroup);
    }

    @Override
    public Thread createThread(Runnable runnable) {
        return this.delegate.createThread(ThreadEnhanceHelper.doEnhance(runnable));
    }

    @Override
    public int getCorePoolSize() {
        return this.delegate.getCorePoolSize();
    }

    @Override
    public void setCorePoolSize(int corePoolSize) {
        this.delegate.setCorePoolSize(corePoolSize);
    }

    @Override
    public int getMaxPoolSize() {
        return this.delegate.getMaxPoolSize();
    }

    @Override
    public void setMaxPoolSize(int maxPoolSize) {
        this.delegate.setMaxPoolSize(maxPoolSize);
    }

    @Override
    public int getKeepAliveSeconds() {
        return this.delegate.getKeepAliveSeconds();
    }

    @Override
    public void setKeepAliveSeconds(int keepAliveSeconds) {
        this.delegate.setKeepAliveSeconds(keepAliveSeconds);
    }

    @Override
    public void setQueueCapacity(int queueCapacity) {
        this.delegate.setQueueCapacity(queueCapacity);
    }

    @Override
    public void setAllowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        this.delegate.setAllowCoreThreadTimeOut(allowCoreThreadTimeOut);
    }

    @Override
    public void setTaskDecorator(TaskDecorator taskDecorator) {
        this.delegate.setTaskDecorator(taskDecorator);
    }

}
