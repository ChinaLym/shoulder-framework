package org.shoulder.core.concurrent;

import org.shoulder.core.concurrent.delay.DelayTask;
import org.shoulder.core.concurrent.delay.DelayTaskHolder;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.log.beautify.LogHelper;
import org.shoulder.core.util.ContextUtils;
import org.springframework.lang.NonNull;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.*;

/**
 * 线程工具类
 * 提供延时任务和常用线程池拒绝策略的封装
 * 注意！该类必须设置线程池之后使用，否则 IllegalStateException！
 *
 * @author lym
 */
public class Threads {

    private static final Logger log = LoggerFactory.getLogger(Threads.class);

    /**
     * shoulder 通用线程池名称
     */
    public final static String SHOULDER_THREAD_POOL_NAME = "shoulderThreadPool";

    /**
     * 通用线程池
     */
    private static ExecutorService SHOULDER_THREAD_POOL;

    /**
     * 延迟任务存放者
     */
    private static DelayTaskHolder DELAY_TASK_HOLDER;


    public static synchronized void setExecutorService(ExecutorService executorService) {
        Threads.SHOULDER_THREAD_POOL = executorService;
        log.info("Threads' DEFAULT_THREAD_POOL has changed to " + executorService);
    }

    public static synchronized void setDelayTaskHolder(DelayTaskHolder delayTaskHolder) {
        Threads.DELAY_TASK_HOLDER = delayTaskHolder;
        log.info("Threads' DELAY_TASK_HOLDER has changed to " + delayTaskHolder);
    }

    /**
     * 使用该方法包装线程类，将自动将线程放入延迟队列并延时执行
     *
     * @param runnable 要延时执行的事情
     * @param time     延时时间
     * @param unit     time 的单位
     * @see #delay(Runnable, Duration) 推荐使用 jdk8 的时间
     */
    public static void delay(Runnable runnable, long time, TimeUnit unit) {
        DelayTask task = new DelayTask(runnable, time, unit);
        delay(task);
    }

    /**
     * 使用该方法包装线程类，将自动将线程放入延迟队列并延时执行
     *
     * @param runnable  要延时执行的事情
     * @param delayTime 延时时间
     */
    public static void delay(Runnable runnable, Duration delayTime) {
        DelayTask task = new DelayTask(runnable, delayTime);
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
        if (log.isDebugEnabled()) {
            StackTraceElement caller = LogHelper.findStackTraceElement(Threads.class, "delay", true);
            String callerName = caller == null ? "" : LogHelper.genCodeLocationLinkFromStack(caller);
            log.debug("{} creat delay task will run in {}ms", callerName, delayTask.getDelay(TimeUnit.MILLISECONDS));
        }
        DELAY_TASK_HOLDER.put(delayTask);
    }

    /**
     * 放入线程池执行
     *
     * @param runnable 要执行的任务
     */
    public static void execute(Runnable runnable) {
        // 是否去掉 null 判断，这里应该认为一定不为空
        if (SHOULDER_THREAD_POOL == null) {
            synchronized (Threads.class) {
                if (SHOULDER_THREAD_POOL == null) {
                    log.warn("not set threadPool fall back: use bean named '{}' in context.", SHOULDER_THREAD_POOL_NAME);
                    Object threadPoolBean = ContextUtils.getBean(SHOULDER_THREAD_POOL_NAME);
                    if (threadPoolBean instanceof ExecutorService) {
                        setExecutorService((ExecutorService) threadPoolBean);
                    }
                    throw new IllegalStateException("Need invoke setExecutorService first!");
                }
            }
        }
        if (log.isDebugEnabled()) {
            StackTraceElement caller = LogHelper.findStackTraceElement(Threads.class, "delay", true);
            String callerName = caller == null ? "" : LogHelper.genCodeLocationLinkFromStack(caller);
            log.debug("{} create a new Thread.", callerName);
        }
        SHOULDER_THREAD_POOL.execute(runnable);
    }

    /**
     * 提交线程池内一批任务，且阻塞至所有的任务执行完毕
     *
     * @param tasks runnable
     * @throws InterruptedException runnable
     */
    public static void executeAndWait(@NonNull Collection<? extends Runnable> tasks) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(tasks.size());
        tasks.forEach(runnable -> execute(new NotifyOnFinishRunnable(runnable, latch::countDown)));
        latch.await();
    }

    /**
     * 提交线程池内一批任务，且阻塞至所有的任务执行完毕
     *
     * @param tasks   executeAndWait
     * @param timeout executeAndWait
     * @return true: 已经全部完成； false: 未全部完成
     * @throws InterruptedException executeAndWait
     */
    public static boolean executeAndWait(@NonNull Collection<? extends Runnable> tasks, Duration timeout)
        throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(tasks.size());
        tasks.forEach(runnable -> execute(new NotifyOnFinishRunnable(runnable, latch::countDown)));
        return latch.await(timeout.getNano(), TimeUnit.NANOSECONDS);
    }


    /**
     * 放入线程池执行
     *
     * @param callable 要执行的任务
     * @return 当前任务执行的 Future
     */
    public static <T> Future<T> submit(Callable<T> callable) {
        if (SHOULDER_THREAD_POOL == null) {
            throw new IllegalStateException("You must setExecutorService first.");
        }
        if (log.isDebugEnabled()) {
            StackTraceElement caller = LogHelper.findStackTraceElement(Threads.class, "delay", true);
            String callerName = caller == null ? "" : LogHelper.genCodeLocationLinkFromStack(caller);
            log.debug("{} submit a new callable.", callerName);
        }
        return SHOULDER_THREAD_POOL.submit(callable);
    }


    public static void shutDown() {
        if (SHOULDER_THREAD_POOL == null) {
            log.info("no threadPool need shutdown.");
            return;
        }
        log.debug("prepare shutdown");
        try {
            SHOULDER_THREAD_POOL.shutdown();
        } catch (Exception e) {
            // on shutDown 钩子可能抛异常
            log.error("shutdown FAIL! - ", e);
        }
        log.info("shutdown SUCCESS.");
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
