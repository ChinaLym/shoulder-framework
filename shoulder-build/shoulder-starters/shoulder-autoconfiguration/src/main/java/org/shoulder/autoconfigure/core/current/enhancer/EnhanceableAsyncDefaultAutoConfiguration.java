package org.shoulder.autoconfigure.core.current.enhancer;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 默认的自动装配
 *
 * @author lym
 */
@AutoConfiguration
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
