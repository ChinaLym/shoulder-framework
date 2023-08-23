package org.shoulder.monitor.concurrent;

/**
 * 可监控的任务
 * 任务（Runnable）为监控指标添加（任务名）标签
 * todo 跟踪进、出队列时间，执行完毕时间；装配等待超时时间...
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

//    String getTaskIdentifier();


}
