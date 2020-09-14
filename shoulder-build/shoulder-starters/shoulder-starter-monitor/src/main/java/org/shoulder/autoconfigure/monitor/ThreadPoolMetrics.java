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

package org.shoulder.autoconfigure.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadPool Metrics Monitor.
 *
 * @author lym
 */
public class ThreadPoolMetrics {


    /**
     * 指标所属模块 标签名
     */
    private static final String TAG_MODULE = "module";

    /**
     * 指标名称 标签名
     */
    private static final String TAG_NAME = "name";

    /**
     * 线程池指标名称
     */
    private final String metricsName;


    /**
     * 中正在执行任务的线程数量
     */
    private AtomicInteger activeCount = new AtomicInteger();

    /**
     * 任务总数（已经执行 + 未执行）
     */
    private AtomicInteger taskCount = new AtomicInteger();

    /**
     * 已完成的任务数量，该值小于等于 taskCount
     */
    private AtomicInteger completedTaskCount = new AtomicInteger();

    /**
     * 队列中的任务数
     */
    private AtomicInteger queueSize = new AtomicInteger();

    /**
     * 队列最大容量
     */
    private AtomicInteger maximumQueueSize = new AtomicInteger();


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


    /**
     *
     * @param metricsName 指标名称（应用对于线程池监控名称的定义），一般格式为 <aapId>_<moduleId>，如 storage_sync_threads
     * @param moduleName 线程池属于哪个模块
     */
    public ThreadPoolMetrics(String metricsName, String moduleName){
        this.metricsName = metricsName;
    }

    /**
     * 
     * @param moduleName 线程池属于哪个模块
     * @param metricsName 应用对于线程池监控名称的定义
     */
    private void registerMetrics(String metricsName, String moduleName) {


        Metrics.gauge(metricsName, List.of(
            new ImmutableTag(TAG_MODULE, moduleName),
            new ImmutableTag(TAG_NAME, "taskCount")
        ), taskCount);

        Metrics.gauge(metricsName, List.of(
            new ImmutableTag(TAG_MODULE, moduleName),
            new ImmutableTag(TAG_NAME, "completedTaskCount")
        ), completedTaskCount);

        
        

        Metrics.gauge(metricsName, List.of(
            new ImmutableTag(TAG_MODULE, moduleName),
            new ImmutableTag(TAG_NAME, "queueSize")
        ), queueSize);

        Metrics.gauge(metricsName, List.of(
            new ImmutableTag(TAG_MODULE, moduleName),
            new ImmutableTag(TAG_NAME, "maximumQueueSize")
        ), maximumQueueSize);

        
        
        Metrics.gauge(metricsName, List.of(
            new ImmutableTag(TAG_MODULE, moduleName),
            new ImmutableTag(TAG_NAME, "activeCount")
        ), activeCount);

        Metrics.gauge(metricsName, List.of(
            new ImmutableTag(TAG_MODULE, moduleName),
            new ImmutableTag(TAG_NAME, "poolSize")
        ), poolSize);
        
        Metrics.gauge(metricsName, List.of(
            new ImmutableTag(TAG_MODULE, moduleName),
            new ImmutableTag(TAG_NAME, "corePoolSize")
        ), corePoolSize);

        Metrics.gauge(metricsName, List.of(
            new ImmutableTag(TAG_MODULE, moduleName),
            new ImmutableTag(TAG_NAME, "maximumPoolSize")
        ), maximumPoolSize);

        Metrics.gauge(metricsName, List.of(
            new ImmutableTag(TAG_MODULE, moduleName),
            new ImmutableTag(TAG_NAME, "largestPoolSize")
        ), largestPoolSize);

    }
    
    public AtomicInteger largestPoolSize() {
        return largestPoolSize;
    }
    
    public AtomicInteger poolSize() {
        return poolSize;
    }
    
    public AtomicInteger maximumPoolSize() {
        return maximumPoolSize;
    }
    
    public AtomicInteger corePoolSize() {
        return corePoolSize;
    }
    
    public AtomicInteger activeCount() {
        return activeCount;
    }
    
    public AtomicInteger taskCount() {
        return taskCount;
    }

    public AtomicInteger queueSize() {
        return queueSize;
    }

    public AtomicInteger maximumQueueSize() {
        return maximumQueueSize;
    }

    public Timer taskExecuteTime() {
        return Metrics.timer("thread_pool_timer", TAG_MODULE, "config", TAG_NAME, "taskExecuteTime");
    }
    
    public Counter exceptionCount() {
        return Metrics.counter("thread_pool_exception", TAG_MODULE, "config", TAG_NAME, "exceptionCount");
    }

    public Counter rejectCount() {
        return Metrics.counter("thread_pool_exception", TAG_MODULE, "config", TAG_NAME, "rejectCount");
    }


}
