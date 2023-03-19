package org.shoulder.autoconfigure.operation;

import org.shoulder.log.operation.logger.OperationLogger;
import org.shoulder.log.operation.support.HashMapOperableObjectTypeRepository;
import org.shoulder.log.operation.support.OperableObjectTypeRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * UserAgent 解析
 *
 * @author lym
 */
@ConditionalOnClass({OperableObjectTypeRepository.class, OperationLogger.class})
@AutoConfiguration
@ConditionalOnProperty(value = "shoulder.log.operation.enable", havingValue = "true", matchIfMissing = true)
public class OperationLogOperableTypeResolverAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OperableObjectTypeRepository operableObjectTypeRepository() {
        return new HashMapOperableObjectTypeRepository(c -> "objectType." + c.getSimpleName());
    }
}
