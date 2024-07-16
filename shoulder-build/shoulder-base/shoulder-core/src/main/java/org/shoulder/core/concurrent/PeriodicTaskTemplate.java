package org.shoulder.core.concurrent;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 周期性任务调度模板
 *
 * @author lym
 */
public class PeriodicTaskTemplate implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PeriodicTaskTemplate.class);

    private final TaskScheduler scheduler;

    private final PeriodicTask task;

    /**
     * 执行计数器
     */
    private final AtomicInteger runCounter = new AtomicInteger(0);

    public PeriodicTaskTemplate(PeriodicTask task, TaskScheduler scheduler) {
        this.task = task;
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        // 执行任务逻辑
        int runCount = runCounter.incrementAndGet();
        try {
            // run
            logger.debug("{} trigger run {}.", task.getTaskName(), runCount);
            task.process();

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
