package org.shoulder.autoconfigure.log.operation.async;

import org.shoulder.log.operation.annotation.OperationLog;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

/**
 * 即使其他地方覆盖了 AsyncConfigurer，也可以激活 OpLog 在 @Async 方法实现跨线程
 * <p>
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * Auto-configuration} that wraps an existing custom {@link AsyncConfigurer} in a
 * {@link OpLogAsyncCustomizer}.
 *
 * @author lym
 */
@Configuration(
    proxyBeanMethods = false
)
@ConditionalOnClass(OperationLog.class)
@ConditionalOnBean(AsyncConfigurer.class)
@AutoConfigureBefore(OpLogAsyncDefaultAutoConfiguration.class)
public class OpLogAsyncCustomAutoConfiguration implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
        throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
        throws BeansException {
        if (bean instanceof AsyncConfigurer
            && !(bean instanceof OpLogAsyncCustomizer)) {
            AsyncConfigurer configurer = (AsyncConfigurer) bean;
            return new OpLogAsyncCustomizer(configurer);
        }
        return bean;
    }

}
