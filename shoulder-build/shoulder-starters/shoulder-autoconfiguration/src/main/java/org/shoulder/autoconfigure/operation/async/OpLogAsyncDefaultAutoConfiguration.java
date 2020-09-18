package org.shoulder.autoconfigure.operation.async;

import org.shoulder.log.operation.annotation.OperationLog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 默认的自动装配
 * 用于支持操作日志线程池中执行时自动跨线程 和 @Async
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(OperationLog.class)
public class OpLogAsyncDefaultAutoConfiguration {

    /**
     * Wrapper for all normal executors.
     */
    @Bean
    public static OpLogExecutorBeanPostProcessor oplogexecutorbeanpostprocessor() {
        return new OpLogExecutorBeanPostProcessor();
    }
}
