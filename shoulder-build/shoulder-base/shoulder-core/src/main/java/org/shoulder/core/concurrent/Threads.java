package org.shoulder.core.concurrent;

import org.shoulder.core.concurrent.enhance.EnhancedRunnable;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.log.beautify.LogHelper;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.core.util.ContextUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * 线程工具类
 * 提供延时任务和常用线程池拒绝策略的封装
 * 注意！该类必须设置线程池之后使用，否则 IllegalStateException！
 *
 * @author lym
 */
public class Threads {

    private static final Logger log = ShoulderLoggers.SHOULDER_THREADS;

    /**
     * shoulder 通用线程池 bean 名称
     */
    public final static String SHOULDER_THREAD_POOL_NAME = "shoulderThreadPool";

    /**
     * Shoulder 通用调度器 bean 名称
     */
    public final static String SHOULDER_TASK_SCHEDULER = "shoulderTaskScheduler";

    /**
     * 执行任务线程池
     */
    static volatile ExecutorService EXECUTOR_SERVICE;

    /**
     * 县城调度器：主要做延迟执行等任务
     */
    static volatile TaskScheduler TASK_SCHEDULER;

    public static synchronized void setExecutorService(ExecutorService executorService) {
        Threads.EXECUTOR_SERVICE = executorService;
        log.debug("Threads' THREAD_POOL has changed to " + executorService);
    }

    public static void setTaskScheduler(TaskScheduler taskScheduler) {
        Threads.TASK_SCHEDULER = taskScheduler;
        log.debug("Threads' TASK_SCHEDULER has changed to " + taskScheduler);
    }

    /**
     * 延迟执行
     *
     * @param taskName      要执行的任务名称
     * @param task          要执行的任务
     * @param delayDuration 延迟的时间
     */
    public static ScheduledFuture<?> delay(@NonNull String taskName, @NonNull Runnable task, @NonNull Duration delayDuration) {
        return schedule(taskName, task, Instant.now().plus(delayDuration), null);
    }

    /**
     * 定期调度执行
     *
     * @param taskName                  要执行的任务名称
     * @param task                      要执行的任务
     * @param firstExecutionTime        （null 或 过去时间 立即执行）
     * @param executionPeriodCalculator 调度间隔计算器 null 只执行一次，否则计算下次执行时间
     */
    public static ScheduledFuture<?> schedule(@NonNull String taskName, @NonNull Runnable task, @NonNull Instant firstExecutionTime, @Nullable BiFunction<Instant, Integer, Instant> executionPeriodCalculator) {
        PeriodicTask periodicTask = new PeriodicTask() {
            @Override
            public String getTaskName() {
                return taskName;
            }

            @Override
            public void process() {
                task.run();
            }

            @Override
            public Instant calculateNextRunTime(Instant now, int runCount) {
                return executionPeriodCalculator == null ? NO_NEED_EXECUTE : executionPeriodCalculator.apply(now, runCount);
            }
        };

        // 执行时放在 EXECUTOR_SERVICE 执行，避免阻塞调度
        return schedule(periodicTask, firstExecutionTime);
    }

    /**
     * 定期调度执行
     *
     * @param periodicTask       要执行的任务
     * @param firstExecutionTime （null 或小与当前时间则立即执行）
     */
    public static ScheduledFuture<?> schedule(PeriodicTask periodicTask, Instant firstExecutionTime) {
        ensureInit();
        if (log.isDebugEnabled()) {
            StackTraceElement caller = LogHelper.findStackTraceElement(Threads.class, "schedule", true);
            String callerName = caller == null ? "" : LogHelper.genCodeLocationLinkFromStack(caller);
            log.debug("{} creat delay task will run at {}", callerName, firstExecutionTime.toEpochMilli());
        }
        return TASK_SCHEDULER.schedule(new PeriodicTaskTemplate(periodicTask, TASK_SCHEDULER), firstExecutionTime);
    }

    /**
     * 放入线程池执行
     *
     * @param runnable 要执行的任务
     */
    public static void execute(Runnable runnable) {
        ensureInit();
        printCallerDebugLog("execute");
        EXECUTOR_SERVICE.execute(runnable);
    }

    public static void execute(String taskName, Runnable runnable) {
        execute(taskName, runnable, null, null);
    }

    /**
     * 异步执行
     *
     * @param taskName           任务名称（线程名）
     * @param runnable           任务
     * @param exceptedFinishTime 预期完成时间
     * @param exceptionCallBack  回调（监督人），预期时间未完成 or 执行出现异常则回调监督人
     */
    public static void execute(String taskName, Runnable runnable, Instant exceptedFinishTime, Consumer<TaskInfo> exceptionCallBack) {
        ensureInit();
        Instant taskSubmitTime = Instant.now();
        AtomicReference<Thread> threadRef = new AtomicReference<>();
        AtomicReference<Exception> errorRef = new AtomicReference<>();
        AtomicReference<Instant> runStartTimeRef = new AtomicReference<>();
        AtomicReference<Instant> runEndTimeRef = new AtomicReference<>();
        AtomicBoolean allowRun = new AtomicBoolean(true);
        AtomicBoolean hasDetected = new AtomicBoolean(false);
        if (log.isTraceEnabled()) {
            log.trace("{} add to EXECUTOR_SERVICE", taskName);
        }
        Runnable detectRun = () -> {
            boolean isFirstDetect = hasDetected.compareAndSet(false, true);
            if (isFirstDetect) {
                exceptionCallBack.accept(new TaskInfo(taskName, taskSubmitTime, runStartTimeRef, runEndTimeRef, Instant.now(), threadRef, errorRef, allowRun));
            }
        };
        EnhancedRunnable enhancedRunnable = new EnhancedRunnable(() -> {
            Instant runStartTime = Instant.now();
            if (!allowRun.get()) {
                log.info("{} execute cancel, wait={}ms.", taskName, Duration.between(taskSubmitTime, runStartTime).toMillis());
            }
            runStartTimeRef.set(runStartTime);
            Thread runThread = Thread.currentThread();
            threadRef.set(runThread);
            String originThreadName = runThread.getName();
            boolean success = false;
            try {
                runThread.setName(taskName);
                runnable.run();
                success = true;
            } catch (Exception e) {
                log.error("{} execute occur Exception! ", taskName, e);
                errorRef.set(e);
                if (exceptionCallBack != null) {
                    // 回调监工
                    runEndTimeRef.set(Instant.now());
                    Threads.execute("D_" + taskName, detectRun, null, null);
                }
                throw e;
            } finally {
                synchronized (runThread) {
                    Instant endTime = Instant.now();
                    runEndTimeRef.set(endTime);
                    // 释放线程引用，避免回调函数执行时，该线程已经在执行其他任务从而引起的误操作和判断
                    threadRef.set(null);
                    log.info("{} execute end, success={}, cost={}ms.", taskName, success, Duration.between(runStartTime, endTime).toMillis());
                    runThread.setName(originThreadName);
                }
            }
        });

        EXECUTOR_SERVICE.execute(enhancedRunnable);

        // 注册监工
        if (exceptionCallBack != null && exceptedFinishTime != null) {
            Threads.schedule("D_" + taskName, detectRun, exceptedFinishTime, null);
        }
    }

    private static void printCallerDebugLog(String methodName) {
        if (log.isDebugEnabled()) {
            StackTraceElement caller = LogHelper.findStackTraceElement(Threads.class, methodName, true);
            //            if(caller!= null && caller.getClassName().startsWith("java")) {
            //                caller = LogHelper.findStackTraceElement(Threads.class, "executeAndWait", true);
            //            }
            String callerName = caller == null ? "" : LogHelper.genCodeLocationLinkFromStack(caller);
            log.debug("{} create new Thread.", callerName);
        }
    }

    private static void ensureInit() {
        // 是否去掉 null 判断，这里应该认为一定不为空
        if (EXECUTOR_SERVICE == null || TASK_SCHEDULER == null) {
            synchronized (Threads.class) {
                if (EXECUTOR_SERVICE == null) {
                    boolean containsBean = ContextUtils.containsBean(SHOULDER_THREAD_POOL_NAME);
                    AssertUtils.isTrue(containsBean, CommonErrorCodeEnum.CODING,
                            "Need invoke setExecutorService first! no fallback threadPool named " + SHOULDER_THREAD_POOL_NAME);

                    Object threadPoolBean = ContextUtils.getBeanOrNull(SHOULDER_THREAD_POOL_NAME);
                    AssertUtils.isTrue(threadPoolBean instanceof ExecutorService, CommonErrorCodeEnum.CODING,
                            "Need invoke setExecutorService first! Error fallback threadPool.class="
                                    + Optional.ofNullable(threadPoolBean).map(Object::getClass).map(Class::getName).orElse(null));

                    log.warn("not set threadPool fall back: try use bean named '{}' in context.", SHOULDER_THREAD_POOL_NAME);
                    setExecutorService((ExecutorService) threadPoolBean);
                }
                if (TASK_SCHEDULER == null) {
                    boolean containsBean = ContextUtils.containsBean(SHOULDER_TASK_SCHEDULER);
                    AssertUtils.isTrue(containsBean, CommonErrorCodeEnum.CODING,
                            "Need invoke setTaskScheduler first! no fallback taskScheduler named " + SHOULDER_TASK_SCHEDULER);

                    Object taskScheduler = ContextUtils.getBeanOrNull(SHOULDER_TASK_SCHEDULER);
                    AssertUtils.isTrue(taskScheduler instanceof TaskScheduler, CommonErrorCodeEnum.CODING,
                            "Need invoke setTaskScheduler first! Error fallback taskScheduler.class="
                                    + Optional.ofNullable(taskScheduler).map(Object::getClass).map(Class::getName).orElse(null));

                    log.warn("not set threadPool fall back: try use bean named '{}' in context.", SHOULDER_TASK_SCHEDULER);
                    setTaskScheduler((TaskScheduler) taskScheduler);
                }
            }
        }
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
        ensureInit();
        printCallerDebugLog("executeAndWait");
        CountDownLatch latch = new CountDownLatch(tasks.size());
        List<Callable<Object>> callList = tasks.stream().map(runnable -> new NotifyOnFinishRunnable(runnable, latch::countDown))
                .map(Executors::callable)
                .toList();
        EXECUTOR_SERVICE.invokeAll(callList, timeout.toNanos(), TimeUnit.NANOSECONDS);
        return latch.await(timeout.toNanos(), TimeUnit.NANOSECONDS);
    }

    /**
     * 放入线程池执行
     *
     * @param callable 要执行的任务
     * @return 当前任务执行的 Future
     */
    public static <T> Future<T> submit(Callable<T> callable) {
        if (EXECUTOR_SERVICE == null) {
            throw new IllegalStateException("You must setExecutorService first.");
        }
        if (log.isDebugEnabled()) {
            StackTraceElement caller = LogHelper.findStackTraceElement(Threads.class, "delay", true);
            String callerName = caller == null ? "" : LogHelper.genCodeLocationLinkFromStack(caller);
            log.debug("{} submit a new callable.", callerName);
        }
        return EXECUTOR_SERVICE.submit(callable);
    }

    public static void shutDown() {
        if (EXECUTOR_SERVICE == null) {
            log.info("no threadPool need shutdown.");
            return;
        }
        log.debug("prepare shutdown");
        try {
            EXECUTOR_SERVICE.shutdown();
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

        private static final Logger log = ShoulderLoggers.SHOULDER_THREADS;

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (!executor.isShutdown()) {
                if (r instanceof FutureTask) {
                    ((FutureTask<?>) r).cancel(true);
                }
            }
            log.warn("Discard for the executor's queue is full. Task({}), Executor({})", r.toString(),
                    executor);
        }
    }

    /**
     * 修复了 jdk 使用 FutureTask 可能一直阻塞的 bug {@link ThreadPoolExecutor.DiscardOldestPolicy}
     */
    public static class DiscardOldest implements RejectedExecutionHandler {

        private static final Logger log = ShoulderLoggers.SHOULDER_THREADS;

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (!executor.isShutdown()) {
                if (r instanceof FutureTask) {
                    ((FutureTask) r).cancel(true);
                }
            }
            log.warn("Discard for the executor's queue is full. Task({}), Executor({})", r.toString(),
                    executor);
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
     * 阻塞调用者（一定时间） + 日志
     */
    public static class Block implements RejectedExecutionHandler {

        private static final Logger log = ShoulderLoggers.SHOULDER_THREADS;

        /**
         * 最长等待时间 null 代表永远阻塞，直至放入
         */
        private final Duration maxWait;

        /**
         * 最长等待时间 null 代表永远阻塞，直至放入
         */
        private final Duration warnWait;

        /**
         * @param maxWait  最长等待时间
         * @param warnWait
         */
        public Block(Duration maxWait, Duration warnWait) {
            this.maxWait = maxWait;
            this.warnWait = warnWait;
        }

        /**
         * 默认一直阻塞，直至放入，阻塞时长超过50ms，则日志由debug转为warn
         */
        public Block() {
            this.maxWait = null;
            this.warnWait = Duration.ofMillis(50);
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (executor.isShutdown()) {
                throw new RejectedExecutionException("Executor has been shutdown");
            }
            try {
                long start = System.currentTimeMillis();
                BlockingQueue<Runnable> queue = executor.getQueue();
                if (maxWait == null) {
                    log.debug("Attempting to queue task execution till success, blocking...");
                    queue.put(r);
                } else {
                    log.debug("Attempting to queue task execution, maxWait: {}ms", this.maxWait.toMillis());
                    if (!queue.offer(r, this.maxWait.toNanos(), TimeUnit.NANOSECONDS)) {
                        throw new RejectedExecutionException("Max wait time(" + this.maxWait.toMillis() + "ms) expired to queue task.");
                    }
                }
                long cost = System.currentTimeMillis() - start;
                if (cost > warnWait.toMillis()) {
                    log.warn("Task queued slowly, cost={}ms", cost);
                } else {
                    log.debug("Task queued, cost={}ms", cost);
                }
            } catch (InterruptedException e) {
                log.debug("Interrupted while queuing task execution");
                Thread.currentThread().interrupt();
                throw new RejectedExecutionException("Interrupted", e);
            }
        }

    }


    // 任务名、执行线程、异常（如果有）、提交任务时间、实际任务执行时间、检测时间
    public record TaskInfo(String taskName, Instant taskSubmitTime, AtomicReference<Instant> runStartTimeRef,
                           AtomicReference<Instant> runEndTimeRef, Instant detectTime,
                           AtomicReference<Thread> threadRef,
                           AtomicReference<Exception> exceptionRef, AtomicBoolean allowRun) {
        /**
         * 对于还没开始执行的任务，可以取消
         *
         * @param interruptRunning 在运行时是否触发中断；对于超时敏感的可以传 true
         * @return 是否取消or中断任务，如果已经成功运行结束，也会返回 false
         */
        public boolean cancelTask(boolean interruptRunning) {
            boolean isCancelled = allowRun.compareAndSet(runStartTimeRef.get() == null, false);
            if (!interruptRunning) {
                return isCancelled;
            }

            Thread thread = threadRef.get();
            synchronized (thread) {
                // DCL + runEndTime 确认正在运行，且这段代码运行时，会确保运行线程没结束
                boolean isRunning = runEndTimeRef.get() != null && threadRef.get() != null;
                if (isRunning) {
                    thread.interrupt();
                    return true;
                }
                return false;
            }
        }
    }
}
