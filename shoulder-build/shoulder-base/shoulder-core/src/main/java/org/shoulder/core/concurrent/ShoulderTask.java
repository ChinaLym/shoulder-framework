package org.shoulder.core.concurrent;

import java.util.concurrent.ExecutorService;

/**
 * 重复执行任务
 */
public interface ShoulderTask {

    /**
     * 任务名称，打印日志使用
     */
    String getTaskName();

    /**
     * 执行内容
     */
    void process();

    default String getExecutorName() {
        return Threads.SHOULDER_THREAD_POOL_NAME;
    }

    default ExecutorService getExecutorService() {
        return Threads.resolveExecutorServiceFromContext(getExecutorName());
    }
}
