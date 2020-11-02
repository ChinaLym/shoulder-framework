package org.shoulder.autoconfigure.monitor.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.shoulder.autoconfigure.monitor.MetricsConst;
import org.shoulder.autoconfigure.monitor.thread.ThreadPoolMetrics;
import org.shoulder.core.context.AppInfo;
import org.springframework.beans.BeansException;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

/**
 * 指标监控装配，推荐依赖 MeterRegistry 而不是 prometheus 的 CollectorRegistry
 *
 * @author lym
 */
@Configuration
public class PrometheusAutoConfiguration implements ApplicationContextAware {

    /**
     * 不用再在配置文件中显示配置 management.metrics.tags.application=${spring.application.name}
     * 但仍需要 management.endpoints.web.exposure.include=*
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> configurer() {
        return (registry) -> registry.config()
            .commonTags(MetricsConst.TAG_APP_ID, AppInfo.appId());
    }

    //@Bean
    public Counter getCounter(MeterRegistry registry) {
        return Counter.builder("thread_pool_execute_count")
            .tags("status", "success")
            .description("Number of successful goods rank sync")
            .register(registry);
    }


    //@Bean
    public MeterFilter renameRegionTagMeterFilter() {
        // 重命名标签
        return MeterFilter.renameTag("org.shoulder", "threadp", "threadpool");
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        String nameKey = "shoulder.metrics.threadPool.name";
        String threadPoolMetricsNamePrefix = applicationContext.getEnvironment()
            .getProperty(nameKey, AppInfo.appId() + "_thread_pool_");
        ThreadPoolMetrics.setDefaultMetricsNamePrefix(threadPoolMetricsNamePrefix);
    }
}
