package org.shoulder.autoconfigure.core.current.enhancer;

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
    @Bean
    public static EnhanceableExecutorBeanPostProcessor enhanceableExecutorBeanPostProcessor() {
        return new EnhanceableExecutorBeanPostProcessor();
    }
}
