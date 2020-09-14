package org.shoulder.autoconfigure.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.shoulder.core.context.AppInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 指标监控装配，推荐依赖 MeterRegistry 而不是 prometheus 的 CollectorRegistry
 *
 * @author lym
 */
@Configuration
public class PrometheusAutoConfiguration {

    /**
     * 不用再在配置文件中显示配置 management.metrics.tags.application=${spring.application.name}
     * 但仍需要 management.endpoints.web.exposure.include=*
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> configurer() {
        return (registry) -> registry.config().commonTags("application", AppInfo.appId());
    }

    @Bean
    public Counter getCounter(@Autowired MeterRegistry registry) {
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

}
