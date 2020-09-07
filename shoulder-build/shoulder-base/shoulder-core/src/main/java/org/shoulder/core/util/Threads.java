package org.shoulder.core.util;

import lombok.extern.shoulder.SLog;
import org.shoulder.core.delay.DelayTask;
import org.shoulder.core.delay.DelayTaskHolder;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.*;

/**
 * 线程工具类
 * 提供延时任务和常用线程池拒绝策略的封装
 * 注意！该类必须设置线程池之后使用，否则 IllegalStateException！
 *
 * @author lym
 */
@SLog
public class Threads {

    public final static String DEFAULT_THREAD_POOL_NAME = "shoulderThreadPool";
    private static ExecutorService DEFAULT_THREAD_POOL;
    private static DelayTaskHolder DELAY_TASK_HOLDER;

    public static void setExecutorService(ExecutorService executorService) {
        Threads.DEFAULT_THREAD_POOL = executorService;
        log.info("Threads' DEFAULT_THREAD_POOL has changed to " + executorService);
    }

    public static void setDelayTaskHolder(DelayTaskHolder delayTaskHolder) {
        Threads.DELAY_TASK_HOLDER = delayTaskHolder;
        log.info("Threads' DELAY_TASK_HOLDER has changed to " + delayTaskHolder);
    }

    /**
     * 使用该方法包装线程类，将自动将线程放入延迟队列并延时执行
     *
     * @param runnable 要延时执行的事情
     * @param time     延时时间
     * @param unit     time 的单位
     */
    public static void delay(Runnable runnable, long time, TimeUnit unit) {
        DelayTask task = new DelayTask(runnable, time, unit);
        delay(task);
    }

    /**
     * 使用该方法包装线程类，将自动将线程放入延迟队列并延时执行
     *
     * @param delayTask 要延时执行的任务
     */
    public static void delay(DelayTask delayTask) {
        if (DELAY_TASK_HOLDER == null) {
            throw new IllegalStateException("You must setDelayTaskHolder first.");
        }
        DELAY_TASK_HOLDER.put(delayTask);
    }

    /**
     * 放入线程池执行
     *
     * @param runnable 要执行的任务
     */
    public static void execute(Runnable runnable) {
        if (DEFAULT_THREAD_POOL == null) {
            throw new IllegalStateException("You must setExecutorService first.");
        }
        DEFAULT_THREAD_POOL.execute(runnable);
    }

    /**
     * 放入线程池执行
     *
     * @param callable 要执行的任务
     * @return 当前任务执行的 Future
     */
    public static <T> Future<T> submit(Callable<T> callable) {
        if (DEFAULT_THREAD_POOL == null) {
            throw new IllegalStateException("You must setExecutorService first.");
        }
        return DEFAULT_THREAD_POOL.submit(callable);
    }


    // ------------------------ Shoulder 的线程池拒绝策略 ------------------------


    /**
     * 修复了 jdk 使用 FutureTask 可能一直阻塞的 bug {@link ThreadPoolExecutor.DiscardPolicy}
     */
    public static class Discard implements RejectedExecutionHandler {

        private static final Logger log = LoggerFactory.getLogger(Discard.class);

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (!executor.isShutdown()) {
                if (r instanceof FutureTask) {
                    ((FutureTask) r).cancel(true);
                }
            }
            log.warn("Discard for the executor's queue is full. Task({}), Executor({})", r.toString(),
                executor.toString());
        }
    }

    /**
     * 修复了 jdk 使用 FutureTask 可能一直阻塞的 bug {@link ThreadPoolExecutor.DiscardOldestPolicy}
     */
    public static class DiscardOldest implements RejectedExecutionHandler {

        private static final Logger log = LoggerFactory.getLogger(DiscardOldest.class);

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (!executor.isShutdown()) {
                if (r instanceof FutureTask) {
                    ((FutureTask) r).cancel(true);
                }
            }
            log.warn("Discard for the executor's queue is full. Task({}), Executor({})", r.toString(),
                executor.toString());
        }
    }

    /**
     * 类比jdk的默认策略，{@link ThreadPoolExecutor.AbortPolicy}，这里将其转为框架的运行时异常，
     *
     * @deprecated use {@link ThreadPoolExecutor.AbortPolicy}
     */
    public static class Abort implements RejectedExecutionHandler {

        private static final Logger log = LoggerFactory.getLogger(Abort.class);

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            throw new RejectedExecutionException("Discard for the executor's queue is full. " +
                "Task(" + r.toString() + "), Executor({" + executor.toString() + "})");
        }
    }


    /**
     * 阻塞调用者策略
     */
    public static class Block implements RejectedExecutionHandler {

        private static final Logger log = LoggerFactory.getLogger(Abort.class);

        /**
         * 最长等待时间 null 代表永远阻塞，直至放入
         */
        private final Duration maxWait;

        /**
         * @param maxWait 最长等待时间
         */
        public Block(Duration maxWait) {
            this.maxWait = maxWait;
        }

        /**
         * 默认一直阻塞，直至放入
         */
        public Block() {
            this.maxWait = null;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (executor.isShutdown()) {
                throw new RejectedExecutionException("Executor has been shut down");
            }
            try {
                BlockingQueue<Runnable> queue = executor.getQueue();
                if (maxWait == null) {
                    log.debug("Attempting to queue task execution till success, blocking...");
                    queue.put(r);
                } else {
                    log.debug("Attempting to queue task execution, maxWait: {}", this.maxWait);
                    if (!queue.offer(r, this.maxWait.toNanos(), TimeUnit.NANOSECONDS)) {
                        throw new RejectedExecutionException("Max wait time expired to queue task");
                    }
                }
                log.debug("Task execution queued");
            } catch (InterruptedException e) {
                log.debug("Interrupted while queuing task execution");
                Thread.currentThread().interrupt();
                throw new RejectedExecutionException("Interrupted", e);
            }
        }

    }

}
