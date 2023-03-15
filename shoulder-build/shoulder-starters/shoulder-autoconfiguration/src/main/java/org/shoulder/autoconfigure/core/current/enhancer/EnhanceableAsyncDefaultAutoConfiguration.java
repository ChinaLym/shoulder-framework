package org.shoulder.autoconfigure.core.current.enhancer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 默认的自动装配
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
public class EnhanceableAsyncDefaultAutoConfiguration {

    /**
     * Wrapper for all normal executors.
     */
    //@Order(Ordered.LOWEST_PRECEDENCE)
    @ConditionalOnProperty(value = "shoulder.threadpool.enhancer.enable", havingValue = "true", matchIfMissing = true)
    @Bean
    public static EnhanceableExecutorBeanPostProcessor enhanceableExecutorBeanPostProcessor() {
        return new EnhanceableExecutorBeanPostProcessor();
    }
}
