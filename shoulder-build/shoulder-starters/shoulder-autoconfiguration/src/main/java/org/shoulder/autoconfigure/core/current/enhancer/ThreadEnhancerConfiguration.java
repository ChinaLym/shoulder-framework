package org.shoulder.autoconfigure.core.current.enhancer;

import org.shoulder.core.concurrent.enhance.AppContextThreadLocalAutoTransferEnhancer;
import org.shoulder.core.concurrent.enhance.ThreadEnhanceHelper;
import org.shoulder.core.concurrent.enhance.ThreadEnhancer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

/**
 * 线程池自动增强
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
public class ThreadEnhancerConfiguration {

    /**
     * shoulder 上下文核心类线程变量自动跨线程增强器
     */
    @Bean
    public ThreadEnhancer appContextThreadLocalAutoTransferEnhancer() {
        return new AppContextThreadLocalAutoTransferEnhancer();
    }

    /**
     * 自动注册
     */
    @Bean
    public BeanPostProcessor threadEnhancePostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(@Nonnull Object bean, String beanName) throws BeansException {
                if (bean instanceof ThreadEnhancer) {
                    ThreadEnhanceHelper.register((ThreadEnhancer) bean);
                }
                return bean;
            }
        };
    }
}
