package org.shoulder.autoconfigure.core;

import org.shoulder.core.util.Threads;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程相关的配置
 *
 * @author lym
 */
@Configuration(
    proxyBeanMethods = false
)
public class ThreadAutoConfiguration {

    @Bean(Threads.DEFAULT_THREAD_POOL_NAME)
    @ConditionalOnMissingBean(name = Threads.DEFAULT_THREAD_POOL_NAME)
    public ExecutorService shoulderThreadPool() {
        // 默认使用 5 个线程
        ExecutorService executorService = Executors.newFixedThreadPool(5,
            r -> {
                Thread thread = Executors.defaultThreadFactory().newThread(r);
                thread.setDaemon(true);
                thread.setName("shoulder");
                return thread;
            }
        );
        Threads.setExecutorService(executorService);
        return executorService;
    }

}
