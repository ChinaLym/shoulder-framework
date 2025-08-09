package org.shoulder.autoconfigure.core.current.enhancer;

import jakarta.annotation.Nonnull;
import org.shoulder.autoconfigure.core.BaseAppProperties;
import org.shoulder.core.concurrent.enhance.AppContextThreadLocalAutoTransferEnhancer;
import org.shoulder.core.concurrent.enhance.ThreadEnhanceHelper;
import org.shoulder.core.concurrent.enhance.ThreadEnhancer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 线程池自动增强
 *
 * @author lym
 */
@AutoConfiguration
public class ThreadEnhancerConfiguration {

    /**
     * Auto inherit threadLocals to async threads and clean automatically
     */
    @Bean
    public ThreadEnhancer appContextThreadLocalAutoTransferEnhancer() {
        return new AppContextThreadLocalAutoTransferEnhancer();
    }

    /**
     * BeanPostProcessor 必须为静态方法、不会在Spring容器初始化阶段过早地引用其他还未准备好的bean。
     */
    @Bean
    @ConditionalOnProperty(name = BaseAppProperties.concurrentEnhanceConfigPath, havingValue = "true", matchIfMissing = true)
    public static BeanPostProcessor threadEnhancePostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
                if (bean instanceof ThreadEnhancer) {
                    ThreadEnhanceHelper.register((ThreadEnhancer) bean);
                }
                return bean;
            }
        };
    }
}
