package org.shoulder.core.concurrent;

import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.util.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

/**
 * 周期性、在线程池中运行的任务
 *
 * @author lym
 */
public interface PeriodicTask extends NamedPoolTask {

    Instant NO_NEED_EXECUTE = null;

    /**
     * 第一次什么时候运行
     *
     * @return null 或小与当前时间则立即执行
     */
    default Instant firstExecutionTime() {
        return Instant.EPOCH;
    }

    /**
     * 计算下次延迟时间的方法
     *
     * @param now      当前运行时间
     * @param runCount 当前已运行次数
     * @return 下次执行时间（null 不再需要执行）
     */
    default Instant calculateNextRunTime(Instant now, int runCount) {
        return NO_NEED_EXECUTE;
    }

    /**
     * 处理异常
     *
     * @param task     任务资深
     * @param runTimes 运行次数
     * @param e        异常
     */
    default void handleException(PeriodicTask task, int runTimes, Exception e) {
        ShoulderLoggers.SHOULDER_THREADS.error("{} error int runTimes={}", task.getTaskName(), runTimes, e);
    }

    static PeriodicTask create(@NonNull String taskName, @NonNull Runnable runnable, @Nullable Instant firstExecutionTime, @Nullable BiFunction<Instant, Integer, Instant> executionPeriodCalculator) {
        return PeriodicTask.create(taskName, runnable, firstExecutionTime, executionPeriodCalculator, null, null);
    }

    static PeriodicTask create(@NonNull String taskName, @NonNull Runnable runnable, @Nullable Instant firstExecutionTime, @Nullable BiFunction<Instant, Integer, Instant> executionPeriodCalculator, @NonNull String executorServiceBeanName) {
        return PeriodicTask.create(taskName, runnable, firstExecutionTime, executionPeriodCalculator, executorServiceBeanName, null);
    }

    static PeriodicTask create(@NonNull String taskName, @NonNull Runnable runnable, @Nullable Instant firstExecutionTime, @Nullable BiFunction<Instant, Integer, Instant> executionPeriodCalculator, @NonNull ExecutorService executorService) {
        return PeriodicTask.create(taskName, runnable, firstExecutionTime, executionPeriodCalculator, null, executorService);
    }

    /**
     * 创建周期性任务
     *
     * @param taskName 任务名
     * @param runnable runnable
     * @param firstExecutionTime 第一次执行时间，若未 null 则立即执行
     * @param executionPeriodCalculator 调度周期计算器，计算下一次执行时间
     * @param executorServiceBeanName 异步执行线程池名称
     * @param executorService 异步执行线程池
     * @return 周期性任务
     */
    static PeriodicTask create(@NonNull String taskName, @NonNull Runnable runnable, @Nullable Instant firstExecutionTime, @Nullable BiFunction<Instant, Integer, Instant> executionPeriodCalculator, @Nullable String executorServiceBeanName, @Nullable ExecutorService executorService) {
        return new PeriodicTask() {
            @Override
            public String getTaskName() {
                return taskName;
            }

            @Override
            public void process() {
                runnable.run();
            }

            @Override
            public Instant firstExecutionTime() {
                return firstExecutionTime;
            }

            @Override
            public Instant calculateNextRunTime(Instant now, int runCount) {
                return executionPeriodCalculator == null ? NO_NEED_EXECUTE : executionPeriodCalculator.apply(now, runCount);
            }

            @Override
            public String getExecutorServiceBeanName() {
                return StringUtils.isEmpty(executorServiceBeanName) ? PeriodicTask.super.getExecutorServiceBeanName() : executorServiceBeanName;
            }

            @Override
            public ExecutorService getExecutorService() {
                return executorService == null ? PeriodicTask.super.getExecutorService() : executorService;
            }
        };
    }

}
