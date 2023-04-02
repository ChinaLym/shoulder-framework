package org.shoulder.monitor.concurrent;

import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Metrics;
import org.shoulder.core.concurrent.delay.DelayQueueDelayTaskHolder;
import org.shoulder.core.concurrent.delay.DelayTask;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 内部封装一个延迟队列，基于阻塞式，适合延迟任务较少的场景
 * 若延迟任务量特别大，推荐通过时间轮方式做
 *
 * @author lym
 */
public class MonitorableDelayTaskHolder extends DelayQueueDelayTaskHolder {

    private final AtomicInteger queueSize = new AtomicInteger();

    public MonitorableDelayTaskHolder(DelayQueue<DelayTask> delayQueue, String monitorKey) {
        super(delayQueue);

        Metrics.gauge(monitorKey, List.of(
                new ImmutableTag("name", "queue")
        ), queueSize);
    }

    /**
     * @param delayTask 已被封装的延时任务
     */
    @Override
    public void put(@Nonnull DelayTask delayTask) {
        super.put(delayTask);
        queueSize.incrementAndGet();
    }

    /**
     * 该方法阻塞调用者，直到有延迟任务达到触发时机
     * 他是一个小气鬼，在他手里拿够了才把延迟任务给别人
     *
     * @return 可执行的任务
     */
    @Nonnull
    @Override
    public DelayTask next() throws InterruptedException {
        DelayTask t = super.next();
        queueSize.decrementAndGet();
        return t;
    }

}
