package org.shoulder.core.concurrent.delay;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.ShoulderLoggers;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 延迟任务能力的默认实现
 * 延时任务调度器，负责从 {@link DelayTaskHolder} 中拿任务，放入线程池中。
 * 该线程在容器初始化完毕后将在线程池中开始进行延迟任务的搬运工作，且长期占用线程池一个线程。
 *
 * @author lym
 */
public class DelayTaskDispatcher implements Runnable {

    private static final Logger log = ShoulderLoggers.SHOULDER_THREADS;

    private static volatile boolean running = false;
    /**
     * 执行任务的默认线程池
     */
    private Executor defaultExecutor;
    /**
     * 延迟任务从哪里取
     */
    private DelayTaskHolder delayTaskHolder;

    public DelayTaskDispatcher(Executor defaultExecutor, DelayTaskHolder delayTaskHolder) {
        this.defaultExecutor = defaultExecutor;
        this.delayTaskHolder = delayTaskHolder;
    }

    /**
     * 启动延迟任务调度线程
     */
    public synchronized void start() {
        if (running) {
            log.warn("invalid operation 'start', already running.");
            return;
        }
        running = true;
        Thread dispatcherThread = Executors.defaultThreadFactory().newThread(this);
        dispatcherThread.setDaemon(true);
        dispatcherThread.setName("shoulderDelayTaskPorter");
        dispatcherThread.start();
    }

    @Override
    public void run() {
        log.info("DelayTaskPorter started.");
        DelayTask delay;
        while (true) {
            try {
                // 从延时队列中获取任务
                delay = delayTaskHolder.next();
                Runnable task = delay.getTask();
                if (task == null) {
                    continue;
                }
                defaultExecutor.execute(task);

            } catch (Exception e) {
                log.error("dispatch delayTask fail", e);
            }
        }
    }

}
