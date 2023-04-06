package org.shoulder.autoconfigure.monitor;

import org.shoulder.autoconfigure.core.current.DelayTaskAutoConfiguration;
import org.shoulder.autoconfigure.core.current.ThreadAutoConfiguration;
import org.shoulder.core.concurrent.Threads;
import org.shoulder.core.concurrent.delay.DelayTaskHolder;
import org.shoulder.core.context.AppInfo;
import org.shoulder.monitor.concurrent.MonitorableDelayTaskHolder;
import org.shoulder.monitor.concurrent.MonitorableThreadPool;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
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
@ConditionalOnClass(MonitorableThreadPool.class)
@AutoConfiguration(before = {ThreadAutoConfiguration.class}, after = DelayTaskAutoConfiguration.class)
public class MonitorableThreadAutoConfiguration {

    @Bean(Threads.SHOULDER_THREAD_POOL_NAME)
    @ConditionalOnMissingBean(name = Threads.SHOULDER_THREAD_POOL_NAME)
    public ExecutorService shoulderThreadPool() {
        // 默认使用 5 个线程
        ThreadPoolExecutor executor = new MonitorableThreadPool(5, 5,
                60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(3000),
                new CustomizableThreadFactory("shoulder"), Threads.SHOULDER_THREAD_POOL_NAME);
        // 提前设置，方便使用
        Threads.setExecutorService(executor);
        return executor;
    }

    @Bean
    @Primary
    @ConditionalOnBean(DelayTaskHolder.class)
    public DelayTaskHolder delayTaskHolder(DelayTaskHolder delegate) {
        DelayTaskHolder delayTaskHolder = new MonitorableDelayTaskHolder(delegate,
            AppInfo.appId() + "_delayTaskHolder");
        Threads.setDelayTaskHolder(delayTaskHolder);
        return delayTaskHolder;
    }

}
