package org.shoulder.autoconfigure.operation;

import org.shoulder.log.operation.async.OperationLogThreadLocalAutoTransferEnhancer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 日志自动跨线程
 *
 * @author lym
 */
@ConditionalOnClass(OperationLogThreadLocalAutoTransferEnhancer.class)
@AutoConfiguration
@ConditionalOnProperty(value = "shoulder.log.operation.enable", havingValue = "true", matchIfMissing = true)
public class OperationLogThreadLocalAutoTransferEnhancerAutoConfiguration {

    @Bean
    public OperationLogThreadLocalAutoTransferEnhancer operationLogThreadLocalAutoTransferEnhancer() {
        return new OperationLogThreadLocalAutoTransferEnhancer();
    }
}
