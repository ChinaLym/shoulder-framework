package org.shoulder.monitor.concurrent;

import org.shoulder.core.concurrent.enhance.EnhancedRunnable;

/**
 * 可监控的任务
 * 任务（Runnable）为监控指标添加（任务名）标签
 *
 * @author lym
 */
public class DefaultMonitorableRunnable extends EnhancedRunnable implements MonitorableRunnable {

    private String taskName;
    private String taskIdentifier;
    private long   enqueueTime;
    private long   waitInQueueDuration;

    public DefaultMonitorableRunnable(Runnable delegate) {
        super(delegate);
    }

    /**
     * Getter method for property <tt>taskName</tt>.
     *
     * @return property value of taskName
     */
    @Override public String getTaskName() {
        return taskName;
    }

    /**
     * Setter method for property <tt>taskName</tt>.
     *
     * @param taskName  value to be assigned to property taskName
     */
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    /**
     * Getter method for property <tt>taskIdentifier</tt>.
     *
     * @return property value of taskIdentifier
     */
    @Override public String getTaskIdentifier() {
        return taskIdentifier;
    }

    /**
     * Setter method for property <tt>taskIdentifier</tt>.
     *
     * @param taskIdentifier  value to be assigned to property taskIdentifier
     */
    public void setTaskIdentifier(String taskIdentifier) {
        this.taskIdentifier = taskIdentifier;
    }

    /**
     * Getter method for property <tt>enqueueTime</tt>.
     *
     * @return property value of enqueueTime
     */
    @Override public long getEnqueueTime() {
        return enqueueTime;
    }

    /**
     * Setter method for property <tt>enqueueTime</tt>.
     *
     * @param enqueueTime  value to be assigned to property enqueueTime
     */
    @Override public void setEnqueueTime(long enqueueTime) {
        this.enqueueTime = enqueueTime;
    }

    /**
     * Getter method for property <tt>waitInQueueDuration</tt>.
     *
     * @return property value of waitInQueueDuration
     */
    @Override public long getWaitInQueueDuration() {
        return waitInQueueDuration;
    }

    /**
     * Setter method for property <tt>waitInQueueDuration</tt>.
     *
     * @param waitInQueueDuration  value to be assigned to property waitInQueueDuration
     */
    @Override public void setWaitInQueueDuration(long waitInQueueDuration) {
        this.waitInQueueDuration = waitInQueueDuration;
    }
}
