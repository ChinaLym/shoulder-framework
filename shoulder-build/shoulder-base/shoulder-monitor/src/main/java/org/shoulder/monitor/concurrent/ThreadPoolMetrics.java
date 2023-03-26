package org.shoulder.monitor.concurrent;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.shoulder.core.concurrent.enhance.EnhancedRunnable;
import org.shoulder.core.util.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 线程池监控指标
 *
 * @author lym
 */
public class ThreadPoolMetrics {

    private static String DEFAULT_METRICS_NAME_PREFIX = "appId_thread_pool_";

    /**
     * 指标所属模块 标签名（线程池名称）
     */
    private static final String TAG_MODULE = "module";

    /**
     * 指标名称 标签名（指标名称，如 core、max）
     */
    private static final String TAG_NAME = "name";

    /**
     * 任务指标
     */
    private static final String TAG_TASK = "task";

    /**
     * 指标名称前缀（应用对于线程池监控名称的定义），一般格式为 <aapId>_<moduleId>
     * 如库存服务，同步业务的线程池监控指标前缀命名 storage_sync_threads_
     */
    private final String metricsNamePrefix = DEFAULT_METRICS_NAME_PREFIX;

    /**
     * 模块名称，默认标签 ThreadPool 名称
     */
    private final String moduleName;


    /**
     * 中正在执行任务的线程数量
     */
    private final AtomicInteger activeCount = new AtomicInteger();

    /**
     * 任务总数（已经执行 + 未执行）
     */
    private final AtomicLong taskCount = new AtomicLong();

    /**
     * 已完成的任务数量，该值小于等于 taskCount
     * AtomicLong 可以 set，LongAdder 没有 set 方法
     */
    private final AtomicLong completedTaskCount = new AtomicLong();

    /**
     * 队列中的任务数（当前待执行的任务数）
     */
    private final AtomicInteger queueSize = new AtomicInteger();

    /**
     * 队列最大容量，可补充统计 剩余容量
     * （也可以在监控配置中写死）
     */
    private final AtomicInteger queueCapacity = new AtomicInteger();


    /**
     * 核心线程数量
     */
    private final AtomicInteger corePoolSize = new AtomicInteger();

    /**
     * 最大线程数量
     */
    private final AtomicInteger maximumPoolSize = new AtomicInteger();

    /**
     * 当前的线程数量（不一定在运行）
     */
    private final AtomicInteger poolSize = new AtomicInteger();

    /**
     * 线程池存在以来最大线程数量。通过该值可以判断是否满过（达到maximumPoolSize）
     */
    private final AtomicInteger largestPoolSize = new AtomicInteger();

    public static String getDefaultMetricsNamePrefix() {
        return DEFAULT_METRICS_NAME_PREFIX;
    }

    public static void setDefaultMetricsNamePrefix(String defaultMetricsNamePrefix) {
        if (!defaultMetricsNamePrefix.endsWith("_")) {
            defaultMetricsNamePrefix = defaultMetricsNamePrefix + "_";
        }
        DEFAULT_METRICS_NAME_PREFIX = defaultMetricsNamePrefix;
    }

    /**
     * 构造器
     *
     * @param moduleName 线程池属于哪个模块，为了对比多个模块，shoulder 默认把模块名放在标签上。（若不比较，也推荐放在指标名中）
     */
    public ThreadPoolMetrics(String moduleName) {
        this.moduleName = moduleName;
        registerMetrics();
    }

    private void registerMetrics() {

        // 任务数（执行数）
        String taskMetricsName = metricsNamePrefix + "tasks";

        Metrics.gauge(taskMetricsName, List.of(
                new ImmutableTag(TAG_MODULE, moduleName),
                new ImmutableTag(TAG_NAME, "total")
        ), taskCount);

        Metrics.gauge(taskMetricsName, List.of(
                new ImmutableTag(TAG_MODULE, moduleName),
                new ImmutableTag(TAG_NAME, "completed")
        ), completedTaskCount);


        // 队列中的任务数
        String queueSizeMetricsName = metricsNamePrefix + "queue_tasks";

        Metrics.gauge(queueSizeMetricsName, List.of(
                new ImmutableTag(TAG_MODULE, moduleName),
                new ImmutableTag(TAG_NAME, "num")
        ), queueSize);

        Metrics.gauge(queueSizeMetricsName, List.of(
                new ImmutableTag(TAG_MODULE, moduleName),
                new ImmutableTag(TAG_NAME, "capacity")
        ), queueCapacity);


        // 线程池中线程数
        String threadMetricsName = metricsNamePrefix + "threads";

        Metrics.gauge(threadMetricsName, List.of(
                new ImmutableTag(TAG_MODULE, moduleName),
                new ImmutableTag(TAG_NAME, "active")
        ), activeCount);

        Metrics.gauge(threadMetricsName, List.of(
                new ImmutableTag(TAG_MODULE, moduleName),
                new ImmutableTag(TAG_NAME, "current")
        ), poolSize);

        Metrics.gauge(threadMetricsName, List.of(
                new ImmutableTag(TAG_MODULE, moduleName),
                new ImmutableTag(TAG_NAME, "core")
        ), corePoolSize);

        Metrics.gauge(threadMetricsName, List.of(
                new ImmutableTag(TAG_MODULE, moduleName),
                new ImmutableTag(TAG_NAME, "max")
        ), maximumPoolSize);

        Metrics.gauge(threadMetricsName, List.of(
                new ImmutableTag(TAG_MODULE, moduleName),
                new ImmutableTag(TAG_NAME, "largest")
        ), largestPoolSize);

    }


    public AtomicInteger corePoolSize() {
        return corePoolSize;
    }

    public AtomicInteger maximumPoolSize() {
        return maximumPoolSize;
    }

    public AtomicInteger queueCapacity() {
        return queueCapacity;
    }

    //----

    public AtomicInteger activeCount() {
        return activeCount;
    }

    public AtomicInteger poolSize() {
        return poolSize;
    }

    public AtomicInteger largestPoolSize() {
        return largestPoolSize;
    }

    //----

    public AtomicLong taskCount() {
        return taskCount;
    }

    public AtomicLong completedTaskCount() {
        return completedTaskCount;
    }

    public AtomicInteger queueSize() {
        return queueSize;
    }

    /**
     * 可根据此值，统计最大、平均、90% 95% 99%、慢任务报警
     */
    public Timer taskExecuteTime() {
        return Metrics.timer(metricsNamePrefix + "timer",
                TAG_MODULE, moduleName,
                TAG_NAME, "execute");
    }

    public Timer taskExecuteTime(String taskName) {
        if (StringUtils.isEmpty(taskName)) {
            return taskExecuteTime();
        }
        return Metrics.timer(metricsNamePrefix + "timer",
                TAG_MODULE, moduleName,
                TAG_NAME, "execute",
                TAG_TASK, moduleName);
    }

    public Timer taskExecuteTime(Runnable runnable) {
        if (runnable instanceof MonitorableRunnable) {
            return taskExecuteTime(((MonitorableRunnable) runnable).getTaskName());
        }
        return taskExecuteTime();
    }

    public Counter exceptionCount() {
        return Metrics.counter(metricsNamePrefix + "exceptions",
                TAG_MODULE, moduleName,
                TAG_NAME, "exception");
    }

    public Counter exceptionCount(String taskName) {
        if (StringUtils.isEmpty(taskName)) {
            return exceptionCount();
        }
        return Metrics.counter(metricsNamePrefix + "exceptions",
                TAG_MODULE, moduleName,
                TAG_NAME, "exception",
                TAG_TASK, moduleName);
    }

    public Counter exceptionCount(Runnable runnable) {
        if (runnable instanceof MonitorableRunnable) {
            return exceptionCount(((MonitorableRunnable) runnable).getTaskName());
        } else if (runnable instanceof EnhancedRunnable && ((EnhancedRunnable) runnable).isInstanceOf(MonitorableRunnable.class)) {
            exceptionCount(((EnhancedRunnable) runnable).as(MonitorableRunnable.class).getTaskName());
        }
        return exceptionCount();
    }


    public Counter rejectCount() {
        return Metrics.counter(metricsNamePrefix + "reject_nums",
                TAG_MODULE, moduleName,
                TAG_NAME, "rejectCount");
    }

    public Counter rejectCount(String taskName) {
        if (StringUtils.isEmpty(taskName)) {
            return rejectCount();
        }
        return Metrics.counter(metricsNamePrefix + "reject_nums",
                TAG_MODULE, moduleName,
                TAG_NAME, "rejectCount",
                TAG_TASK, moduleName);
    }

    public Counter rejectCount(Runnable runnable) {
        if (runnable instanceof MonitorableRunnable) {
            return rejectCount(((MonitorableRunnable) runnable).getTaskName());
        }
        return rejectCount();
    }

}
