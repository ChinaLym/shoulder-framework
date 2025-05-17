package org.shoulder.autoconfigure.operation;

import jakarta.annotation.Nonnull;
import org.shoulder.log.operation.format.OperationLogParamValueConverter;
import org.shoulder.log.operation.format.covertor.DefaultOperationLogParamValueConverter;
import org.shoulder.log.operation.format.covertor.OperationLogParamValueConverterHolder;
import org.shoulder.log.operation.model.OperationLogDTO;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;

import java.util.Collection;

/**
 * 日志参数转换器
 *
 * @author lym
 */
@AutoConfiguration
@ConditionalOnClass(OperationLogDTO.class)
@EnableConfigurationProperties(OperationLogProperties.class)
@ConditionalOnProperty(value = "shoulder.log.operation.enable", havingValue = "true", matchIfMissing = true)
public class OperationLogParamConverterAutoConfiguration implements ApplicationContextAware {


    /**
     * 默认的 value 解析器
     */
    @Bean
    @ConditionalOnMissingBean(OperationLogParamValueConverter.class)
    public static DefaultOperationLogParamValueConverter defaultOperationLogParamValueConverter(OperationLogProperties operationLogProperties) {
        return new DefaultOperationLogParamValueConverter(operationLogProperties.getNullParamOutput());
    }

    /**
     * 初始化 {@link OperationLogParamValueConverterHolder}
     */
    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        Collection<OperationLogParamValueConverter> allConverters = applicationContext.getBeansOfType(OperationLogParamValueConverter.class).values();
        DefaultOperationLogParamValueConverter defaultConverter = applicationContext.getBean(DefaultOperationLogParamValueConverter.class);
        OperationLogParamValueConverterHolder.init(allConverters, defaultConverter);
    }
}
