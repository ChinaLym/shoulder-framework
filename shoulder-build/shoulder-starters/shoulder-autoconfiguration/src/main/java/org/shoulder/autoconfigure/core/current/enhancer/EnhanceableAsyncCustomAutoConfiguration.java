package org.shoulder.autoconfigure.core.current.enhancer;

import jakarta.annotation.Nonnull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.AsyncConfigurer;

/**
 * 即使其他地方覆盖了 AsyncConfigurer，也可以激活 shoulder 线程池增强能力
 * <p>
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * Auto-configuration} that wraps an existing custom {@link AsyncConfigurer} in a
 * {@link EnhanceableAsyncCustomizer}.
 *
 * @author lym
 */
@AutoConfiguration(before = EnhanceableAsyncDefaultAutoConfiguration.class)
@ConditionalOnBean(AsyncConfigurer.class)
public class EnhanceableAsyncCustomAutoConfiguration implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(@Nonnull Object bean, String beanName)
        throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(@Nonnull Object bean, String beanName)
        throws BeansException {
        if (bean instanceof AsyncConfigurer
            && !(bean instanceof EnhanceableAsyncCustomizer)) {
            AsyncConfigurer configurer = (AsyncConfigurer) bean;
            return new EnhanceableAsyncCustomizer(configurer);
        }
        return bean;
    }

}
