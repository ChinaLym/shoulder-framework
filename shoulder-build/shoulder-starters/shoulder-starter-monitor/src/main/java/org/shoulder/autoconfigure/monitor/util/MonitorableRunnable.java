package org.shoulder.autoconfigure.monitor.util;

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


}
