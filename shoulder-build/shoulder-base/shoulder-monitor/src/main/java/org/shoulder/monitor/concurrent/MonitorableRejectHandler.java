package org.shoulder.monitor.concurrent;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 带有监控能力的 RejectedExecutionHandler
 * 因为 jdk 的 ExecutorService 的 reject 方法被 final 覆盖了，因此通过装饰者模式采集拒绝次数数据
 *
 * @author lym
 */
public class MonitorableRejectHandler implements RejectedExecutionHandler {


    private RejectedExecutionHandler delegate;


    private ThreadPoolMetrics threadPoolMetrics;

    public MonitorableRejectHandler(RejectedExecutionHandler delegate, ThreadPoolMetrics threadPoolMetrics) {
        this.delegate = delegate;
        this.threadPoolMetrics = threadPoolMetrics;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        threadPoolMetrics.rejectCount(r).increment();
        delegate.rejectedExecution(r, executor);
    }

}
