package org.shoulder.monitor.concurrent;

import lombok.Getter;
import lombok.Setter;
import org.shoulder.core.concurrent.enhance.EnhancedRunnable;

/**
 * 可监控的任务
 * 任务（Runnable）为监控指标添加（任务名）标签
 *
 * @author lym
 */
@Getter
@Setter
public class DefaultMonitorableRunnable extends EnhancedRunnable implements MonitorableRunnable {

    private String taskName;
    private String runnableId;
    private long   enqueueTime;
    private long   waitInQueueDuration;

    public DefaultMonitorableRunnable(Runnable delegate) {
        super(delegate);
    }

}
