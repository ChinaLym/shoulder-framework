package org.shoulder.core.concurrent.enhance;

import io.micrometer.common.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.CustomizableThreadCreator;
import org.springframework.util.ErrorHandler;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Wrap Spring 任务调度器
 *
 * @author lym
 */
public class EnhanceableThreadPoolTaskScheduler extends ThreadPoolTaskScheduler implements EnhanceableExecutorMark {

	private static final Map<ThreadPoolTaskScheduler, EnhanceableThreadPoolTaskScheduler> CACHE = new ConcurrentHashMap<>();

	private final ThreadPoolTaskScheduler delegate;

	private final Method initializeExecutor;

	private final Method createExecutor;

	private final Method cancelRemainingTask;

	private final Method nextThreadName;

	private final Method getDefaultThreadNamePrefix;

	EnhanceableThreadPoolTaskScheduler(ThreadPoolTaskScheduler delegate) {
		this.delegate = delegate;
		this.initializeExecutor = ReflectionUtils.findMethod(ThreadPoolTaskScheduler.class, "initializeExecutor", null);
		makeAccessibleIfNotNull(this.initializeExecutor);
		this.createExecutor = ReflectionUtils.findMethod(ThreadPoolTaskScheduler.class, "createExecutor", null);
		makeAccessibleIfNotNull(this.createExecutor);
		this.cancelRemainingTask = ReflectionUtils.findMethod(ThreadPoolTaskScheduler.class, "cancelRemainingTask",
				null);
		makeAccessibleIfNotNull(this.cancelRemainingTask);
		this.nextThreadName = ReflectionUtils.findMethod(ThreadPoolTaskScheduler.class, "nextThreadName", null);
		makeAccessibleIfNotNull(this.nextThreadName);
		this.getDefaultThreadNamePrefix = ReflectionUtils.findMethod(CustomizableThreadCreator.class,
				"getDefaultThreadNamePrefix", null);
		makeAccessibleIfNotNull(this.getDefaultThreadNamePrefix);
	}

	/**
	 * Wraps the Executor in a trace instance.
	 *
	 * @param delegate delegate to wrap
	 */
	static EnhanceableThreadPoolTaskScheduler wrap(@NonNull ThreadPoolTaskScheduler delegate) {
		return CACHE.computeIfAbsent(delegate,
				e -> new EnhanceableThreadPoolTaskScheduler(delegate));
	}

	private void makeAccessibleIfNotNull(@Nullable Method method) {
		if (method != null) {
			ReflectionUtils.makeAccessible(method);
		}
	}

	@Override
	public void setPoolSize(int poolSize) {
		this.delegate.setPoolSize(poolSize);
	}

	@Override
	public void setRemoveOnCancelPolicy(boolean removeOnCancelPolicy) {
		this.delegate.setRemoveOnCancelPolicy(removeOnCancelPolicy);
	}

	@Override
	public void setErrorHandler(ErrorHandler errorHandler) {
		this.delegate.setErrorHandler(errorHandler);
	}

	@Override
	public ExecutorService initializeExecutor(ThreadFactory threadFactory,
											  RejectedExecutionHandler rejectedExecutionHandler) {
		ExecutorService executorService = (ExecutorService) ReflectionUtils.invokeMethod(this.initializeExecutor,
				this.delegate, traceThreadFactory(threadFactory),
				traceRejectedExecutionHandler(rejectedExecutionHandler));
		if (executorService instanceof EnhanceableScheduledExecutorService) {
			return executorService;
		}
		return EnhanceableExecutorService.wrap(executorService);
	}

	private RejectedExecutionHandler traceRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
		return new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				rejectedExecutionHandler.rejectedExecution(ThreadEnhanceHelper.doEnhance(r), executor);
			}
		};
	}

	private ThreadFactory traceThreadFactory(ThreadFactory threadFactory) {
		return r -> threadFactory.newThread(ThreadEnhanceHelper.doEnhance(r));
	}

	@Override
	public ScheduledExecutorService createExecutor(int poolSize, ThreadFactory threadFactory,
			RejectedExecutionHandler rejectedExecutionHandler) {
		ScheduledExecutorService executorService = (ScheduledExecutorService) ReflectionUtils.invokeMethod(
				this.createExecutor, this.delegate, poolSize, traceThreadFactory(threadFactory),
				traceRejectedExecutionHandler(rejectedExecutionHandler));
		if (executorService instanceof EnhanceableScheduledExecutorService) {
			return executorService;
		}
		return EnhanceableScheduledExecutorService.wrap(executorService);
	}

	@Override
	public ScheduledExecutorService getScheduledExecutor() throws IllegalStateException {
		ScheduledExecutorService executor = this.delegate.getScheduledExecutor();
		return executor instanceof EnhanceableScheduledExecutorService ? executor
				: EnhanceableScheduledExecutorService.wrap(executor);
	}

	@Override
	public ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor() throws IllegalStateException {
		ScheduledThreadPoolExecutor executor = this.delegate.getScheduledThreadPoolExecutor();
		if (executor instanceof EnhanceableScheduledThreadPoolExecutor) {
			return executor;
		}
		return EnhanceableScheduledThreadPoolExecutor.wrap(executor.getCorePoolSize(), executor.getThreadFactory(),
				executor.getRejectedExecutionHandler(), executor);
	}

	@Override
	public int getPoolSize() {
		return this.delegate.getPoolSize();
	}

	@Override
	public boolean isRemoveOnCancelPolicy() {
		return this.delegate.isRemoveOnCancelPolicy();
	}

	@Override
	public int getActiveCount() {
		return this.delegate.getActiveCount();
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
	public ListenableFuture<?> submitListenable(Runnable task) {
		return this.delegate.submitListenable(ThreadEnhanceHelper.doEnhance(task));
	}

	@Override
	public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
		return this.delegate.submitListenable(ThreadEnhanceHelper.doEnhance(task));
	}

	@Override
	public void cancelRemainingTask(Runnable task) {
		ReflectionUtils.invokeMethod(this.cancelRemainingTask, this.delegate, ThreadEnhanceHelper.doEnhance(task));
	}

	@Override
	public boolean prefersShortLivedTasks() {
		return this.delegate.prefersShortLivedTasks();
	}

	@Override
	@Nullable
	public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
		return this.delegate.schedule(ThreadEnhanceHelper.doEnhance(task), trigger);
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable task, Date startTime) {
		return this.delegate.schedule(ThreadEnhanceHelper.doEnhance(task), startTime);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
		return this.delegate.scheduleAtFixedRate(ThreadEnhanceHelper.doEnhance(task), startTime, period);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
		return this.delegate.scheduleAtFixedRate(ThreadEnhanceHelper.doEnhance(task), period);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
		return this.delegate.scheduleWithFixedDelay(ThreadEnhanceHelper.doEnhance(task), startTime, delay);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
		return this.delegate.scheduleWithFixedDelay(ThreadEnhanceHelper.doEnhance(task), delay);
	}

	@Override
	public void setThreadFactory(@Nullable ThreadFactory threadFactory) {
		this.delegate.setThreadFactory(threadFactory);
	}

	@Override
	public void setThreadNamePrefix(@Nullable String threadNamePrefix) {
		this.delegate.setThreadNamePrefix(threadNamePrefix);
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
	public void afterPropertiesSet() {
		this.delegate.afterPropertiesSet();
	}

	@Override
	public void initialize() {
		this.delegate.initialize();
	}

	@Override
	public void destroy() {
		this.delegate.destroy();
	}

	@Override
	public void shutdown() {
		this.delegate.shutdown();
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
	public void setThreadPriority(int threadPriority) {
		this.delegate.setThreadPriority(threadPriority);
	}

	@Override
	public int getThreadPriority() {
		return this.delegate.getThreadPriority();
	}

	@Override
	public void setDaemon(boolean daemon) {
		this.delegate.setDaemon(daemon);
	}

	@Override
	public boolean isDaemon() {
		return this.delegate.isDaemon();
	}

	@Override
	public void setThreadGroupName(String name) {
		this.delegate.setThreadGroupName(name);
	}

	@Override
	public void setThreadGroup(@Nullable ThreadGroup threadGroup) {
		this.delegate.setThreadGroup(threadGroup);
	}

	@Override
	@Nullable
	public ThreadGroup getThreadGroup() {
		return this.delegate.getThreadGroup();
	}

	@Override
	public Thread createThread(Runnable runnable) {
		return this.delegate.createThread(ThreadEnhanceHelper.doEnhance(runnable));
	}

	@Override
	public String nextThreadName() {
		return (String) ReflectionUtils.invokeMethod(this.nextThreadName, this.delegate);
	}

	@Override
	public String getDefaultThreadNamePrefix() {
		if (this.delegate == null) {
			return super.getDefaultThreadNamePrefix();
		}
		return (String) ReflectionUtils.invokeMethod(this.getDefaultThreadNamePrefix, this.delegate);
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
		return this.delegate.schedule(ThreadEnhanceHelper.doEnhance(task), startTime);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Instant startTime, Duration period) {
		return this.delegate.scheduleAtFixedRate(ThreadEnhanceHelper.doEnhance(task), startTime, period);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) {
		return this.delegate.scheduleAtFixedRate(ThreadEnhanceHelper.doEnhance(task), period);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay) {
		return this.delegate.scheduleWithFixedDelay(ThreadEnhanceHelper.doEnhance(task), startTime, delay);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) {
		return this.delegate.scheduleWithFixedDelay(ThreadEnhanceHelper.doEnhance(task), delay);
	}

}