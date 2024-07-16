package org.shoulder.core.concurrent;

import org.shoulder.core.log.Logger;

import java.time.Instant;

/**
 * 重复执行任务
 */
public interface PeriodicTask {

    Instant NO_NEED_EXECUTE = null;

    /**
     * 任务明，打印日志使用
     */
    String getTaskName();

    /**
     * 执行内容
     */
    void process();

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
     * @param logger   日志
     * @param runCount 运行轮次
     * @param e        异常
     */
    default void handleException(Logger logger, int runCount, Exception e) {
        logger.error("{} error int runCount={}", getTaskName(), runCount, e);
    }
}
