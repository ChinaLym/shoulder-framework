package org.shoulder.autoconfigure.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import jakarta.annotation.Nonnull;
import org.shoulder.core.context.AppInfo;
import org.shoulder.monitor.MetricsConst;
import org.shoulder.monitor.concurrent.ThreadPoolMetrics;
import org.springframework.beans.BeansException;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;

/**
 * 指标监控装配，推荐依赖 MeterRegistry 而不是 prometheus 的 CollectorRegistry
 * <p>
 * 访问： http://localhost:8080/actuator/prometheus
 * 带参数 http://localhost:8080/actuator/prometheus?includedNames=n1,n2
 *
 * @author lym
 * @see PrometheusScrapeEndpoint
 */
@ConditionalOnClass(MetricsConst.class)
@AutoConfiguration
public class PrometheusAutoConfiguration implements ApplicationContextAware {

    public PrometheusAutoConfiguration() {
        // just for debug
    }

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
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        String nameKey = "shoulder.metrics.threadPool.name";
        String threadPoolMetricsNamePrefix = applicationContext.getEnvironment()
            .getProperty(nameKey, AppInfo.appId() + "_thread_pool_");
        ThreadPoolMetrics.setDefaultMetricsNamePrefix(threadPoolMetricsNamePrefix);
    }
}
