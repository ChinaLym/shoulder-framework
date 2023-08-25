package java;

import org.shoulder.core.concurrent.BaseDecorateableBlockingQueue;
import org.shoulder.core.concurrent.enhance.EnhancedRunnable;
import org.shoulder.monitor.concurrent.MonitorableRunnable;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * MonitorableBlockingQueue 不需要使用 MonitorableRunnable 包装 runnable，
 * 即可获得统计队列待的平均时间能力（默认FIFO队列很准，但定制的优先队列会方差比较大）；但不能精确到单个任务在队列的耗时
 * fixme 测试用，暂不上线
 */
public class MonitorableBlockingQueue<E> extends BaseDecorateableBlockingQueue<E> {

    private ConcurrentLinkedQueue<Long> enqueueTimeQueue = new ConcurrentLinkedQueue<>();

    public MonitorableBlockingQueue(BlockingQueue<E> delegateBlockingQueue) {
        super(delegateBlockingQueue);
        // delegateBlockingQueue.remainingCapacity() 元素少可以用 ringbuffer 加速
    }

    protected void enQueue(E e) {
        enqueueTimeQueue.offer(System.currentTimeMillis());
    }

    protected E deQueue(E e) {
        long latestEnqueueTime = enqueueTimeQueue.remove();
        long avgInQueueTime = System.currentTimeMillis() - latestEnqueueTime;
//        return ThreadPoolMetrics.;
        return e;

    }

}
