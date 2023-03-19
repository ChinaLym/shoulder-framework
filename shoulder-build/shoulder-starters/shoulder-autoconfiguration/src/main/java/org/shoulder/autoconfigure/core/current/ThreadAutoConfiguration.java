package org.shoulder.autoconfigure.core.current;

import org.shoulder.core.concurrent.Threads;
import org.shoulder.core.util.ContextUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
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
@AutoConfiguration
public class ThreadAutoConfiguration {

    @Bean(Threads.SHOULDER_THREAD_POOL_NAME)
    @ConditionalOnMissingBean(name = Threads.SHOULDER_THREAD_POOL_NAME)
    public ExecutorService shoulderThreadPool() {
        // 默认使用 5 个线程，非守护线程
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 5,
                60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(3000),
                new CustomizableThreadFactory("shoulder"));
        // 提前设置，方便使用
        Threads.setExecutorService(executor);
        return executor;
    }


    /**
     * 自动注册，保证 Threads 中使用的是经过增强的
     */
    @Bean
    public ApplicationListener<ContextRefreshedEvent> shoulderThreadsUtilPostProcessor() {
        return event -> Threads.setExecutorService(ContextUtils.getBean(Threads.SHOULDER_THREAD_POOL_NAME));
    }

}
