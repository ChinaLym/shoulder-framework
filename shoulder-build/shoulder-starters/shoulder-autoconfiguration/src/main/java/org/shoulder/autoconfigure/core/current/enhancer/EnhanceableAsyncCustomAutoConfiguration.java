package org.shoulder.autoconfigure.core.current.enhancer;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import javax.annotation.Nonnull;

/**
 * 即使其他地方覆盖了 AsyncConfigurer，也可以激活 shoulder 线程池增强能力
 * <p>
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * Auto-configuration} that wraps an existing custom {@link AsyncConfigurer} in a
 * {@link EnhanceableAsyncCustomizer}.
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(AsyncConfigurer.class)
@AutoConfigureBefore(EnhanceableAsyncDefaultAutoConfiguration.class)
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
