package org.shoulder.monitor.concurrent;

import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Metrics;
import jakarta.annotation.Nonnull;
import org.shoulder.core.concurrent.delay.DelayTask;
import org.shoulder.core.concurrent.delay.DelayTaskHolder;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 包了一层延迟队列的数量
 *
 * @author lym
 */
public class MonitorableDelayTaskHolder implements DelayTaskHolder {

    private final DelayTaskHolder delegate;

    private final AtomicInteger queueSize = new AtomicInteger();

    public MonitorableDelayTaskHolder(DelayTaskHolder delegate, String monitorKey) {
        this.delegate = delegate;
        Metrics.gauge(monitorKey, List.of(
                new ImmutableTag("name", "queue")
        ), queueSize);
    }

    @Override
    public void put(@Nonnull DelayTask delayTask) {
        delegate.put(delayTask);
        queueSize.incrementAndGet();
    }

    @Nonnull
    @Override
    public DelayTask next() throws Exception {
        DelayTask t = delegate.next();
        queueSize.decrementAndGet();
        return t;
    }

}
