package org.shoulder.monitor.concurrent;

import lombok.Getter;
import lombok.Setter;
import org.shoulder.core.concurrent.enhance.EnhancedCallable;

import java.util.concurrent.Callable;

/**
 * 可监控的任务
 * 任务（Runnable）为监控指标添加（任务名）标签
 *
 * @author lym
 */
@Getter
@Setter
public class DefaultMonitorableCallable<V> extends EnhancedCallable<V> implements MonitorableRunnable {

    private String taskName;
    private String runnableId;
    private long   enqueueTime;
    private long   waitInQueueDuration;

    public DefaultMonitorableCallable(Callable<V> delegate) {
        super(delegate);
    }

}
