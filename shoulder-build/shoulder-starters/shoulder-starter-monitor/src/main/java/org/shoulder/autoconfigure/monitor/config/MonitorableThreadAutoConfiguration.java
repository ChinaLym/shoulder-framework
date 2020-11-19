package org.shoulder.autoconfigure.monitor.config;

import org.shoulder.autoconfigure.monitor.thread.MonitorableThreadPool;
import org.shoulder.core.util.Threads;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 线程相关的配置
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
public class MonitorableThreadAutoConfiguration {

    @Bean(Threads.DEFAULT_THREAD_POOL_NAME)
    public ExecutorService shoulderThreadPool() {
        // 默认使用 5 个线程
        ExecutorService executorService = new MonitorableThreadPool(5, 5,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(3000),
            r -> {
                Thread thread = Executors.defaultThreadFactory().newThread(r);
                thread.setDaemon(true);
                thread.setName("shoulder");
                return thread;
            }, Threads.DEFAULT_THREAD_POOL_NAME);
        Threads.setExecutorService(executorService);
        return executorService;
    }

}
