package org.shoulder.core.thread;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.*;

/**
 * Shoulder 的线程池拒绝策略
 *
 * @author lym
 */
public class ShoulderRejectedExecutionHandlers {

    private static final Logger log = LoggerFactory.getLogger(ShoulderRejectedExecutionHandlers.class);

    /**
     * 修复了 jdk 使用 FutureTask 可能一直阻塞的 bug {@link ThreadPoolExecutor.DiscardPolicy}
     */
    public static class Discard implements RejectedExecutionHandler {

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
     * 类比jdk的：{@link ThreadPoolExecutor.AbortPolicy}，jdk的默认策略，这里将其转为框架异常
     *
     * @deprecated use {@link ThreadPoolExecutor.AbortPolicy}
     */
    public static class Abort implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            throw new RejectedExecutionException("Discard for the executor's queue is full. " +
                "Task(" + r.toString() + "), Executor({" + executor.toString() + "})");
        }
    }


    /**
     * 阻塞调用者策略
     */
    public class Block implements RejectedExecutionHandler {

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
                if(maxWait == null){
                    log.debug("Attempting to queue task execution till success, blocking...");
                    queue.put(r);
                }else {
                    log.debug("Attempting to queue task execution, maxWait: {}", this.maxWait);
                    if (!queue.offer(r, this.maxWait.getNano(), TimeUnit.NANOSECONDS)) {
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
