package org.shoulder.core.concurrent.delay;

import jakarta.annotation.Nonnull;
import org.shoulder.core.concurrent.Threads;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.ShoulderLoggers;

import java.util.concurrent.DelayQueue;

/**
 * 内部封装一个延迟队列，基于阻塞式，适合延迟任务较少的场景
 * 若延迟任务量特别大，推荐通过时间轮方式做
 *
 * @deprecated 1.0 已被 {@link Threads#schedule} 替代
 *
 * @author lym
 */
public class DelayQueueDelayTaskHolder implements DelayTaskHolder {

    protected static final Logger log = ShoulderLoggers.SHOULDER_THREADS;

    /**
     * 延迟队列，默认不限制大小，需要限制可自定义
     */
    private final DelayQueue<DelayTask> DELAY_QUEUE;

    public DelayQueueDelayTaskHolder(DelayQueue<DelayTask> delayQueue) {
        DELAY_QUEUE = delayQueue;
    }

    /**
     * @param delayTask 已被封装的延时任务
     */
    @Override
    public void put(@Nonnull DelayTask delayTask) {
        DELAY_QUEUE.put(delayTask);
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
        return DELAY_QUEUE.take();
    }

}
