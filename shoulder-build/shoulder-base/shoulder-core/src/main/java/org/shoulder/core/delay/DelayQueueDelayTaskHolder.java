package org.shoulder.core.delay;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.DelayQueue;

/**
 * 内部封装一个延迟队列，基于阻塞式，适合延迟任务较少的场景
 * 若延迟任务量特别大，推荐通过时间轮方式做
 *
 * @author lym
 */
public class DelayQueueDelayTaskHolder implements DelayTaskHolder {

    private static final Logger log = LoggerFactory.getLogger(DelayQueueDelayTaskHolder.class);

    /**
     * 延迟队列
     */
    private static final DelayQueue<DelayTask> DELAY_QUEUE = new DelayQueue<>();

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
