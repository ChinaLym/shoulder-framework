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
@Configuration(
    proxyBeanMethods = false
)
@ConditionalOnClass(OperationLog.class)
public class OpLogAsyncDefaultAutoConfiguration {

    /**  Wrapper for all normal executors.*/
    @Bean
    public static OpLogExecutorBeanPostProcessor oplogexecutorbeanpostprocessor() {
        return new OpLogExecutorBeanPostProcessor();
    }
}
