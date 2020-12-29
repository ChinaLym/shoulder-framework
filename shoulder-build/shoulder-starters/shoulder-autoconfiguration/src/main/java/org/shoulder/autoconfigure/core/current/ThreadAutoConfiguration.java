package org.shoulder.autoconfigure.core.current;

import org.shoulder.core.concurrent.Threads;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程相关的配置
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
public class ThreadAutoConfiguration {

    @Bean(Threads.SHOULDER_THREAD_POOL_NAME)
    @ConditionalOnMissingBean(name = Threads.SHOULDER_THREAD_POOL_NAME)
    public ExecutorService shoulderThreadPool() {
        // 默认使用 5 个线程，非守护线程
        return new ThreadPoolExecutor(5, 5,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(3000),
            new CustomizableThreadFactory("shoulder"));
    }


    /**
     * 自动注册
     */
    @Bean
    public BeanPostProcessor threadEnhancePostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(@Nonnull Object bean, String beanName) throws BeansException {
                if (bean instanceof ExecutorService && Threads.SHOULDER_THREAD_POOL_NAME.equals(beanName)) {
                    Threads.setExecutorService((ExecutorService) bean);
                }
                return bean;
            }
        };
    }

}
