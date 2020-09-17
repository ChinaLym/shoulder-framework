/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.shoulder.autoconfigure.monitor.errorcode;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Metrics;
import org.shoulder.autoconfigure.monitor.thread.MonitorableRunnable;
import org.shoulder.core.util.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 错误码监控指标
 *
 * @author lym
 */
public class ErrorCodeMetrics {

    private static String DEFAULT_METRICS_NAME_PREFIX = "appId_error_code_";

    /**
     * 指标所属模块 标签名
     */
    private static final String TAG_MODULE = "module";

    /**
     * 指标名称 标签名
     */
    private static final String TAG_NAME = "name";

    /**
     * 具体错误码 标签名
     */
    private static final String CODE_TASK = "code";

    /**
     * 指标名称前缀（应用对于错误码监控名称的定义），一般格式为 <aapId>_<moduleId>，如 storage_sync_error_
     */
    private final String metricsNamePrefix = DEFAULT_METRICS_NAME_PREFIX;

    /**
     * 模块名称，默认标签
     */
    private final String moduleName;


    /**
     * 中正在执行任务的线程数量
     */
    private AtomicInteger activeCount = new AtomicInteger();

    /**
     * 任务总数（已经执行 + 未执行）
     */
    private AtomicLong taskCount = new AtomicLong();

    /**
     * 已完成的任务数量，该值小于等于 taskCount
     * AtomicLong 可以 set，LongAdder 没有 set 方法
     */
    private AtomicLong completedTaskCount = new AtomicLong();

    /**
     * 队列中的任务数（当前待执行的任务数）
     */
    private AtomicInteger queueSize = new AtomicInteger();

    /**
     * 队列最大容量，可补充统计 剩余容量
     * （也可以在监控配置中写死）
     */
    private AtomicInteger queueCapacity = new AtomicInteger();


    /**
     * 核心线程数量
     */
    private AtomicInteger corePoolSize = new AtomicInteger();

    /**
     * 最大线程数量
     */
    private AtomicInteger maximumPoolSize = new AtomicInteger();

    /**
     * 当前的线程数量（不一定在运行）
     */
    private AtomicInteger poolSize = new AtomicInteger();

    /**
     * 线程池存在以来最大线程数量。通过该值可以判断是否满过（达到maximumPoolSize）
     */
    private AtomicInteger largestPoolSize = new AtomicInteger();

    public static String getDefaultMetricsNamePrefix() {
        return DEFAULT_METRICS_NAME_PREFIX;
    }

    public static void setDefaultMetricsNamePrefix(String defaultMetricsNamePrefix) {
        if(!defaultMetricsNamePrefix.endsWith("_")){
            defaultMetricsNamePrefix = defaultMetricsNamePrefix + "_";
        }
        DEFAULT_METRICS_NAME_PREFIX = defaultMetricsNamePrefix;
    }

    /**
     * 构造器
     *
     * @param moduleName 线程池属于哪个模块，为了对比多个模块，shoulder 默认把模块名放在标签上。（若不比较，也推荐放在指标名中）
     */
    public ErrorCodeMetrics(String moduleName){
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

    public Counter exceptionCount() {
        return Metrics.counter(metricsNamePrefix + "exceptions",
            TAG_MODULE, moduleName,
            TAG_NAME, "exception");
    }
    public Counter exceptionCount(String taskName) {
        if(StringUtils.isEmpty(taskName)){
            return exceptionCount();
        }
        return Metrics.counter(metricsNamePrefix + "exceptions",
            TAG_MODULE, moduleName,
            TAG_NAME, "exception",
            CODE_TASK, moduleName);
    }

    public Counter exceptionCount(Runnable runnable) {
        if(runnable instanceof MonitorableRunnable){
            return exceptionCount(((MonitorableRunnable) runnable).getTaskName());
        }
        return exceptionCount();
    }


    public Counter rejectCount() {
        return Metrics.counter(metricsNamePrefix + "reject_nums",
            TAG_MODULE, moduleName,
            TAG_NAME, "rejectCount");
    }

    public Counter rejectCount(String taskName) {
        if(StringUtils.isEmpty(taskName)){
            return rejectCount();
        }
        return Metrics.counter(metricsNamePrefix + "reject_nums",
            TAG_MODULE, moduleName,
            TAG_NAME, "rejectCount",
            CODE_TASK, moduleName);
    }

    public Counter rejectCount(Runnable runnable) {
        if(runnable instanceof MonitorableRunnable){
            return rejectCount(((MonitorableRunnable) runnable).getTaskName());
        }
        return rejectCount();
    }

}
