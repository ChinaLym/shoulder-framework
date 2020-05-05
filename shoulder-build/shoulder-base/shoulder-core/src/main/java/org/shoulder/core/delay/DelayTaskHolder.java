package org.shoulder.core.delay;

import org.shoulder.core.util.Threads;

import java.util.concurrent.DelayQueue;

/**
 * 延时任务持有者
 * 内部封装一个延迟队列
 * 使用：直接调用静态方法 put 进去即可。推荐：{@link Threads#delay}使用工具类
 *
 * @author lym
 */
public class DelayTaskHolder {

    /** 开关 */
    private static volatile boolean enable = true;

    /** 延迟队列 */
    private static final DelayQueue<DelayTask> DELAY_QUEUE = new DelayQueue<>();

    /**
     * @param delayTask 已被封装的延时任务
     */
    public static void put(DelayTask delayTask) {
        if(enable){
            DELAY_QUEUE.put(delayTask);
        }
        throw new IllegalStateException("taskHolder is already close!");
    }

    /**
     * 该方法可能会使调用者阻塞，till 有延迟任务到期
     * 延时任务持有者是一个小气鬼，直到在自己手里拿够了才给调用方，否则阻塞调用方
     * @return 可执行的任务
     */
    static DelayTask next() throws InterruptedException {
        return DELAY_QUEUE.take();
    }

    /**
     * 停止接收任务
     */
    public static void close(){
        enable = false;
    }

}
