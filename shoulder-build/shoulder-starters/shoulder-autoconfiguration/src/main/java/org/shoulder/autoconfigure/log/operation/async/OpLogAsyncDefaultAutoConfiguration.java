package org.shoulder.autoconfigure.log.operation.async;

import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.async.executors.OpLogExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;

import java.util.concurrent.Executor;

/**
 * 默认的自动装配
 * 用于支持操作日志线程池中执行时自动跨线程 和 @Async
 * @author lym
 */
@Configuration
@ConditionalOnClass(OperationLog.class)
public class OpLogAsyncDefaultAutoConfiguration {

    /**  Wrapper for all normal executors.*/
    @Bean
    public static OpLogExecutorBeanPostProcessor OpLogExecutorBeanPostProcessor() {
        return new OpLogExecutorBeanPostProcessor();
    }


    /** Wrapper for the async executor.
     * @deprecated 已经包装了所有的线程池，无需只针对 @Async 的线程池再次包装
     */
    //@Configuration
    //@ConditionalOnMissingBean({AsyncConfigurer.class})
    //@AutoConfigureBefore(AsyncDefaultAutoConfiguration.class)
    /** sleuth 会自动再包装一次 */
    static class DefaultAsyncConfigurerSupport extends AsyncConfigurerSupport {
        private static final Logger log =
                LoggerFactory.getLogger(OpLogAsyncDefaultAutoConfiguration.DefaultAsyncConfigurerSupport.class);

        @Autowired
        private BeanFactory beanFactory;

        DefaultAsyncConfigurerSupport() {
        }

        @Override
        public Executor getAsyncExecutor() {
            // 1. 拿到用户配置的线程池
            Executor delegate = this.getDefaultExecutor();
            // 2. 包装
            return new OpLogExecutor(delegate);
        }

        /** 获取用户当前配置的线程池 */
        private Executor getDefaultExecutor() {
            try {
                return this.beanFactory.getBean(TaskExecutor.class);
            } catch (NoUniqueBeanDefinitionException var5) {
                log.debug("Could not find unique TaskExecutor bean", var5);

                try {
                    return this.beanFactory.getBean("taskExecutor", Executor.class);
                } catch (NoSuchBeanDefinitionException var3) {
                    if (log.isInfoEnabled()) {
                        log.info("More than one TaskExecutor bean found within the context, and none is named 'taskExecutor'. Mark one of them as primary or name it 'taskExecutor' (possibly as an alias) in order to use it for async processing: " + var5.getBeanNamesFound());
                    }
                }
            } catch (NoSuchBeanDefinitionException var6) {
                log.debug("Could not find default TaskExecutor bean", var6);

                try {
                    return this.beanFactory.getBean("taskExecutor", Executor.class);
                } catch (NoSuchBeanDefinitionException var4) {
                    log.info("No task executor bean found for async processing: no bean of type TaskExecutor and no bean named 'taskExecutor' either");
                }
            }

            if (log.isInfoEnabled()) {
                log.info("For backward compatibility, will fallback to the default, SimpleAsyncTaskExecutor implementation");
            }

            return new SimpleAsyncTaskExecutor();
        }
    }
}
