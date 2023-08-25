package org.shoulder.monitor.concurrent;

import org.shoulder.core.concurrent.BaseDecorateableBlockingQueue;
import org.shoulder.core.concurrent.enhance.EnhancedRunnable;

import java.util.concurrent.BlockingQueue;

/**
 * 帮助 MonitorableRunnable 统计队列内待的时间
 *
 * @author lym
 */
public class MonitorableBlockingQueue extends BaseDecorateableBlockingQueue<Runnable> {

    public MonitorableBlockingQueue(BlockingQueue<Runnable> delegateBlockingQueue) {
        super(delegateBlockingQueue);
        // delegateBlockingQueue.remainingCapacity() 元素少可以用 ringbuffer 加速
    }

    protected void beforeInQueue(Runnable e) {
        EnhancedRunnable.asOptional(e, MonitorableRunnable.class).ifPresent(r -> r.setEnqueueTime(System.currentTimeMillis()));
    }

    protected Runnable afterOutQueue(Runnable r) {
        EnhancedRunnable.asOptional(r, MonitorableRunnable.class).ifPresent(mr -> {
            long enqueueTime = mr.getEnqueueTime();
            long waitInQueueDuration = enqueueTime <= 0 ? 0 : System.currentTimeMillis() - enqueueTime;
            mr.setWaitInQueueDuration(waitInQueueDuration);
        });
        return r;

    }

}
