package org.shoulder.core.concurrent.enhance;

import org.springframework.lang.Nullable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

/**
 * todo delete?
 */
public class EnhanceableTaskScheduler implements TaskScheduler, EnhanceableExecutorMark {

    private final TaskScheduler delegate;

    public EnhanceableTaskScheduler(TaskScheduler delegate) {
        this.delegate = delegate;
    }

    @Nullable
    @Override
    public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
        return delegate.schedule(ThreadEnhanceHelper.doEnhance(task), trigger);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
        return delegate.schedule(ThreadEnhanceHelper.doEnhance(task), startTime);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Instant startTime, Duration period) {
        return delegate.scheduleAtFixedRate(ThreadEnhanceHelper.doEnhance(task), startTime, period);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) {
        return delegate.scheduleAtFixedRate(ThreadEnhanceHelper.doEnhance(task), period);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay) {
        return delegate.scheduleWithFixedDelay(ThreadEnhanceHelper.doEnhance(task), startTime, delay);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) {
        return delegate.scheduleWithFixedDelay(ThreadEnhanceHelper.doEnhance(task), delay);
    }
}
