package org.shoulder.core.concurrent;

import org.shoulder.core.log.ShoulderLoggers;

import java.time.Instant;

/**
 * 重复执行任务
 */
public interface ShoulderCallbackTask extends ShoulderTask {


    /**
     * 期待的完成时间，如果在这个时间未完成则需要中断
     *
     * @return 最晚完成时间点
     */
    default Instant exceptedFinishTime() {
        return null;
    }

    /**
     * 处理异常
     *
     * @param task     任务资深
     * @param e        异常
     */
    default void handleException(Threads.TaskInfo task, Exception e) {
        ShoulderLoggers.SHOULDER_THREADS.error("{} execute exception. submitTime={}, runStartTime={}", task.taskName(), task.taskSubmitTime(), task.runStartTimeRef().get(), e);
    }
    /**
     * 处理超时
     *
     * @param task     任务资深
     * @param e        异常
     */
    default void handleTimeout(Threads.TaskInfo task) {
        ShoulderLoggers.SHOULDER_THREADS.error("{} timeout. submitTime={}, runStartTime={}", task.taskName(), task.taskSubmitTime(), task.runStartTimeRef().get());
    }

    /**
     * 处理任务被取消
     *
     * @param task     任务资深
     * @param e        异常
     */
    default void handleCancelled(Threads.TaskInfo task) {
        ShoulderLoggers.SHOULDER_THREADS.info("{} execute canceled.", task.taskName());
    }

}
