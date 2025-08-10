package org.shoulder.core.concurrent;

import lombok.Getter;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.util.TraceIdGenerator;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 周期性任务调度模板
 *
 * @author lym
 */
public class ShoulderPeriodicTaskRunnable implements Runnable {

    private static final Logger logger = ShoulderLoggers.SHOULDER_THREADS;

    @Getter
    private final TaskScheduler scheduler;

    private final Executor executor;

    @Getter
    private final ShoulderPeriodicTask task;

    @Getter
    private final String relatedTraceId;

    /**
     * 执行计数器
     */
    private final AtomicInteger runTimesCounter = new AtomicInteger(0);

    public ShoulderPeriodicTaskRunnable(ShoulderPeriodicTask task, TaskScheduler scheduler, Executor executor) {
        this.task = task;
        this.scheduler = scheduler;
        this.executor = executor;
        this.relatedTraceId = AppContext.getTraceId();
    }

    @Override
    public void run() {
        // 1. 清空上下文
        AppContext.clean();
        AppContext.setRelatedTraceId(relatedTraceId);
        AppContext.setRelatedTraceId(TraceIdGenerator.generateTraceWithLocalIpV4());

        int runCount = runTimesCounter.incrementAndGet();
        try {
            logger.debug("{} trigger run {} in {} mode. relatedTraceId={}", task.getTaskName(), runCount, executor == null ? "sync" : "async", relatedTraceId);
            // 2. 执行任务逻辑
            if (executor == null) {
                task.process();
            } else {
                executor.execute(task::process);
            }

            // 3. 计算下次执行的延迟时间
            Instant nextRunTime = task.calculateNextRunTime(Instant.now(), runCount);
            if (nextRunTime == ShoulderPeriodicTask.NO_NEED_EXECUTE) {
                logger.debug("{} Terminal after run {}.", task.getTaskName(), runCount);
                return;
            }
            logger.debug("{} next runtime: {}.", task.getTaskName(), nextRunTime);

            // 4. 安排下一次任务调度
            scheduler.schedule(this, nextRunTime);
        } catch (Exception e) {
            task.handleException(task, runCount, e);
        }
    }

}
