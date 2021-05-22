package org.shoulder.autoconfigure.core.current;

import org.shoulder.core.concurrent.Threads;
import org.shoulder.core.concurrent.delay.DelayQueueDelayTaskHolder;
import org.shoulder.core.concurrent.delay.DelayTaskDispatcher;
import org.shoulder.core.concurrent.delay.DelayTaskHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;

/**
 * 延迟任务相关配置
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "shoulder.delayTask.enable", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(DelayTaskProperties.class)
public class DelayTaskAutoConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    @Lazy
    @Autowired
    private DelayTaskDispatcher delayTaskDispatcher;

    @Bean
    @ConditionalOnMissingBean
    public DelayTaskHolder delayTaskHolder() {
        DelayTaskHolder delayTaskHolder = new DelayQueueDelayTaskHolder();
        Threads.setDelayTaskHolder(delayTaskHolder);
        return delayTaskHolder;
    }

    @Bean
    @DependsOn(Threads.SHOULDER_THREAD_POOL_NAME)
    @ConditionalOnProperty(name = "shoulder.delayTask.defaultDispatcher.enable", havingValue = "true", matchIfMissing = true)
    public DelayTaskDispatcher delayTaskPorter(@Qualifier(Threads.SHOULDER_THREAD_POOL_NAME) Executor defaultExecutor,
                                               DelayTaskHolder delayTaskHolder) {
        return new DelayTaskDispatcher(defaultExecutor, delayTaskHolder);
    }

    @Override
    public void onApplicationEvent(@Nonnull ContextRefreshedEvent event) {
        delayTaskDispatcher.start();
    }

}
