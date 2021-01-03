package org.shoulder.autoconfigure.operation;

import org.shoulder.log.operation.async.OperationLogThreadLocalAutoTransferEnhancer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 日志自动跨线程
 *
 * @author lym
 */
@ConditionalOnClass(OperationLogThreadLocalAutoTransferEnhancer.class)
@Configuration(proxyBeanMethods = false)
public class OperationLogThreadLocalAutoTransferEnhancerAutoConfiguration {

    @Bean
    public OperationLogThreadLocalAutoTransferEnhancer operationLogThreadLocalAutoTransferEnhancer() {
        return new OperationLogThreadLocalAutoTransferEnhancer();
    }
}
