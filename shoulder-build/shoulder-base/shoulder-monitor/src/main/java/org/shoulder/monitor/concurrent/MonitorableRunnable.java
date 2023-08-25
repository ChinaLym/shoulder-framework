package org.shoulder.monitor.concurrent;

/**
 * 可监控的任务
 * 任务（Runnable）为监控指标添加（任务名）标签
 *
 * @author lym
 */
public interface MonitorableRunnable {

    /**
     * 任务名称，设置后可以分任务监控
     *
     * @return 任务名称，默认返回类名
     */
    default String getTaskName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 跟踪任务id
     */
    String getTaskIdentifier();

    /**
     * 跟踪进、出队列时间
     */
    void setEnqueueTime(long enqueueTime);

    long getEnqueueTime();

    long getWaitInQueueDuration();
    void setWaitInQueueDuration(long enqueueTime);

}
