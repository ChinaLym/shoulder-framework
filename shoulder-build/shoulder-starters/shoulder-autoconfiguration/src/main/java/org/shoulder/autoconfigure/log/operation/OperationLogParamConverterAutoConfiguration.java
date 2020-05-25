package org.shoulder.autoconfigure.log.operation;

import org.shoulder.log.operation.covertor.DefaultOperationLogParamValueConverter;
import org.shoulder.log.operation.covertor.OperationLogParamValueConverter;
import org.shoulder.log.operation.covertor.OperationLogParamValueConverterHolder;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * 日志参数转换器
 *
 * @author lym
 */
@Configuration
@EnableConfigurationProperties(OperationLogProperties.class)
public class OperationLogParamConverterAutoConfiguration implements ApplicationContextAware {


    /**
     * 默认的 value 解析器
     */
    @Bean
    public DefaultOperationLogParamValueConverter defaultOperationLogParamValueConverter(OperationLogProperties operationLogProperties) {

        return new DefaultOperationLogParamValueConverter(operationLogProperties.getNullParamOutput());
    }

    /**
     * 初始化 {@link OperationLogParamValueConverterHolder}
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Collection<OperationLogParamValueConverter> allConverters = applicationContext.getBeansOfType(OperationLogParamValueConverter.class).values();
        DefaultOperationLogParamValueConverter defaultConverter = applicationContext.getBean(DefaultOperationLogParamValueConverter.class);
        OperationLogParamValueConverterHolder.init(allConverters, defaultConverter);
    }
}
