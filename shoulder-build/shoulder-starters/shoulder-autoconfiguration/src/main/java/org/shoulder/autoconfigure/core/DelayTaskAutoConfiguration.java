package org.shoulder.autoconfigure.core;

import org.shoulder.core.delay.DelayQueueDelayTaskHolder;
import org.shoulder.core.delay.DelayTasDispatcher;
import org.shoulder.core.delay.DelayTaskHolder;
import org.shoulder.core.util.Threads;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;

import java.util.concurrent.Executor;

/**
 * 延迟任务相关配置
 * @author lym
 */
@Configuration
@ConditionalOnProperty(name = "shoulder.delayTask.enable", havingValue = "true", matchIfMissing = false)
public class DelayTaskAutoConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private DelayTasDispatcher delayTasDispatcher;

    @Bean
    @ConditionalOnMissingBean
    public DelayTaskHolder delayTaskHolder(){
        return new DelayQueueDelayTaskHolder();
    }

    @Bean
    @DependsOn(Threads.DEFAULT_THREAD_POOL_NAME)
    @ConditionalOnProperty(name = "shoulder.delayTask.defaultDispatcher.enable", havingValue = "true", matchIfMissing = true)
    public DelayTasDispatcher delayTaskPorter(@Qualifier(Threads.DEFAULT_THREAD_POOL_NAME) Executor defaultExecutor,
                                              DelayTaskHolder delayTaskHolder) {
        return new DelayTasDispatcher(defaultExecutor, delayTaskHolder);
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            delayTasDispatcher.start();
        }
    }

}
