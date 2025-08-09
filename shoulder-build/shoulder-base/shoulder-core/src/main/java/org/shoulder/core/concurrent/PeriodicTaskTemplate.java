package org.shoulder.core.concurrent;

import org.shoulder.core.context.AppContext;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 周期性任务调度模板
 *
 * @author lym
 */
public class PeriodicTaskTemplate implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PeriodicTaskTemplate.class);

    private final TaskScheduler scheduler;

    private final Executor executor;

    private final PeriodicTask task;

    private final String relatedTraceId;

    /**
     * 执行计数器
     */
    private final AtomicInteger runCounter = new AtomicInteger(0);

    public PeriodicTaskTemplate(PeriodicTask task, TaskScheduler scheduler, Executor executor) {
        this.task = task;
        this.scheduler = scheduler;
        this.executor = executor;
        this.relatedTraceId = AppContext.getTraceId();
    }

    @Override
    public void run() {
        // 清空上下文
        AppContext.clean();
        AppContext.setRelatedTraceId(relatedTraceId);

        int runCount = runCounter.incrementAndGet();
        try {
            logger.debug("{} trigger run {} in {} mode. relatedTraceId={}", task.getTaskName(), runCount, executor == null ? "sync" : "async", relatedTraceId);
            // 执行任务逻辑
            if (executor == null) {
                task.process();
            } else {
                executor.execute(task::process);
            }

        } catch (Exception e) {
            task.handleException(logger, runCount, e);
        }

        try {
            // 计算下次执行的延迟时间
            Instant nextRunTime = task.calculateNextRunTime(Instant.now(), runCount);
            if (nextRunTime == PeriodicTask.NO_NEED_EXECUTE) {
                logger.debug("{} Terminal after run {}.", task.getTaskName(), runCount);
                return;
            }
            logger.debug("{} next runtime: {}.", task.getTaskName(), nextRunTime);

            // 安排下一次任务
            scheduler.schedule(this, nextRunTime);
        } catch (Exception e) {
            task.handleException(logger, runCount, e);
        }

    }

}
