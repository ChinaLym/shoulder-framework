package org.shoulder.autoconfigure.core.current;

import org.shoulder.core.concurrent.Threads;
import org.shoulder.core.util.ContextUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程相关的配置
 *
 * @author lym
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@AutoConfiguration
public class ThreadAutoConfiguration {

    @Bean(Threads.SHOULDER_THREAD_POOL_NAME)
    @ConditionalOnMissingBean(name = Threads.SHOULDER_THREAD_POOL_NAME)
    public ExecutorService shoulderThreadPool() {
        // 默认使用 5 个线程，非守护线程
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 5,
                60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(3000),
                new CustomizableThreadFactory("sd-executor-"));
        // 提前设置，方便在启动时使用
        Threads.setExecutorService(executor);
        // 异步预热
        executor.execute(executor::prestartAllCoreThreads);
        return executor;
    }

    @Bean(Threads.SHOULDER_TASK_SCHEDULER)
    @ConditionalOnMissingBean(name = Threads.SHOULDER_TASK_SCHEDULER)
    public ThreadPoolTaskScheduler shoulderTaskScheduler() {
        // 默认使用 5 个线程，非守护线程
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(5);
        taskScheduler.setRemoveOnCancelPolicy(true);
        taskScheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(true);
        taskScheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        taskScheduler.setAcceptTasksAfterContextClose(true);
        taskScheduler.setDaemon(true);
        taskScheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskScheduler.setThreadFactory(new CustomizableThreadFactory("sd-sTask-"));

        // 提前设置，方便在启动时使用
        Threads.setTaskScheduler(taskScheduler);
        return taskScheduler;
    }


    /**
     * 自动注册，保证 Threads 中使用的是经过增强的
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ApplicationListener<ContextRefreshedEvent> shoulderThreadsUtilPostProcessor() {
        return event -> {
            Threads.setExecutorService((ExecutorService) ContextUtils.getBean(Threads.SHOULDER_THREAD_POOL_NAME));
        };
    }

}
