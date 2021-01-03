package org.shoulder.autoconfigure.monitor;

import org.shoulder.autoconfigure.core.current.ThreadAutoConfiguration;
import org.shoulder.core.concurrent.Threads;
import org.shoulder.monitor.concurrent.MonitorableThreadPool;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 线程相关的配置
 *
 * @author lym
 */
@ConditionalOnClass(MonitorableThreadPool.class)
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(ThreadAutoConfiguration.class)
public class MonitorableThreadAutoConfiguration {

    @Bean(Threads.SHOULDER_THREAD_POOL_NAME)
    @ConditionalOnMissingBean(name = Threads.SHOULDER_THREAD_POOL_NAME)
    public ExecutorService shoulderThreadPool() {
        // 默认使用 5 个线程
        return new MonitorableThreadPool(5, 5,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(3000),
            new CustomizableThreadFactory("shoulder"), Threads.SHOULDER_THREAD_POOL_NAME);
    }

}
