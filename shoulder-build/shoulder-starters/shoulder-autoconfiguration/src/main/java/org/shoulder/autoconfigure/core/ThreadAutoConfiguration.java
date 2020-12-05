package org.shoulder.autoconfigure.core;

import org.shoulder.core.concurrent.Threads;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程相关的配置
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
public class ThreadAutoConfiguration {

    @Bean(Threads.DEFAULT_THREAD_POOL_NAME)
    @ConditionalOnMissingBean(name = Threads.DEFAULT_THREAD_POOL_NAME)
    public ExecutorService shoulderThreadPool() {
        // 默认使用 5 个线程，非守护线程
        ExecutorService executorService = new ThreadPoolExecutor(5, 5,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(3000),
            new CustomizableThreadFactory("shoulder"));
        Threads.setExecutorService(executorService);
        return executorService;
    }

}
