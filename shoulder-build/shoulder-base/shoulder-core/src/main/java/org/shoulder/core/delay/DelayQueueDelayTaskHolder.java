package org.shoulder.core.delay;

import lombok.extern.shoulder.SLog;
import org.springframework.lang.NonNull;

import java.util.concurrent.DelayQueue;

/**
 * 内部封装一个延迟队列
 *
 * @author lym
 */
@SLog
public class DelayQueueDelayTaskHolder implements DelayTaskHolder {

    /**
     * 延迟队列
     */
    private static final DelayQueue<DelayTask> DELAY_QUEUE = new DelayQueue<>();

    /**
     * @param delayTask 已被封装的延时任务
     */
    @Override
    public void put(@NonNull DelayTask delayTask) {
        DELAY_QUEUE.put(delayTask);
    }

    /**
     * 该方法阻塞调用者，直到有延迟任务达到触发时机
     * 他是一个小气鬼，在他手里拿够了才把延迟任务给别人
     *
     * @return 可执行的任务
     */
    @NonNull
    @Override
    public DelayTask next() throws InterruptedException {
        return DELAY_QUEUE.take();
    }

}
