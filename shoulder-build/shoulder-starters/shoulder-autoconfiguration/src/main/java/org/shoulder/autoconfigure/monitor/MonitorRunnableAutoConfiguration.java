package org.shoulder.autoconfigure.monitor;

import org.shoulder.monitor.concurrent.MonitorRunnableEnhancer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 【功能】将所有 runnable 封装为 monitorableRunnable
 *
 * @author lym
 */
@ConditionalOnClass(MonitorRunnableEnhancer.class)
@AutoConfiguration
@ConditionalOnProperty(value = "shoulder.monitor.enhancer.enable", havingValue = "true", matchIfMissing = true)
public class MonitorRunnableAutoConfiguration {

    @Bean
    public MonitorRunnableEnhancer enqueueTimeEnhancer() {
        return new MonitorRunnableEnhancer();
    }

}
