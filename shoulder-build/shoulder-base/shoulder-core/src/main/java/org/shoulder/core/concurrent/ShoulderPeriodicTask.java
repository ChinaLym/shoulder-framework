package org.shoulder.core.concurrent;

import org.shoulder.core.log.ShoulderLoggers;

import java.time.Instant;

/**
 * 重复执行任务
 */
public interface ShoulderPeriodicTask extends ShoulderTask {

    Instant NO_NEED_EXECUTE = null;

    /**
     * 第一次什么时候运行
     *
     * @return null 或小与当前时间则立即执行
     */
    default Instant firstExecutionTime() {
        return null;
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
    default void handleException(ShoulderPeriodicTask task, int runTimes, Exception e) {
        ShoulderLoggers.SHOULDER_THREADS.error("{} error int runTimes={}", task.getTaskName(), runTimes, e);
    }

}
