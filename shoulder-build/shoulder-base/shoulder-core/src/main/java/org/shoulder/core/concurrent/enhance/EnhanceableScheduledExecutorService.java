package org.shoulder.core.concurrent.enhance;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 支持 ScheduledExecutorService、ScheduledThreadPoolExecutor、ThreadPoolTaskExecutor、ThreadPoolTaskScheduler
 *
 * @author lym
 */
public class EnhanceableScheduledExecutorService extends EnhanceableExecutorService implements ScheduledExecutorService, EnhanceableExecutorMark {

	private static final Map<ExecutorService, EnhanceableScheduledExecutorService> CACHE = new ConcurrentHashMap<>();

	public EnhanceableScheduledExecutorService(final ExecutorService delegate) {
		super(delegate);
	}

	/**
	 * Wraps the Executor in a trace instance.
	 *
	 * @param delegate delegate to wrap
	 */
	public static EnhanceableScheduledExecutorService wrap(ExecutorService delegate) {
		return CACHE.computeIfAbsent(delegate,
				e -> new EnhanceableScheduledExecutorService(delegate));
	}

	private ScheduledExecutorService getScheduledExecutorService() {
		return (ScheduledExecutorService) this.delegate;
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		return getScheduledExecutorService().schedule(ThreadEnhanceHelper.doEnhance(command), delay, unit);
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		return getScheduledExecutorService().schedule(ThreadEnhanceHelper.doEnhance(callable), delay, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return getScheduledExecutorService()
				.scheduleAtFixedRate(ThreadEnhanceHelper.doEnhance(command), initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		return getScheduledExecutorService()
				.scheduleWithFixedDelay(ThreadEnhanceHelper.doEnhance(command), initialDelay, delay, unit);
	}

}
