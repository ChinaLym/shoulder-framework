package org.shoulder.autoconfigure.log.operation;

import org.shoulder.log.operation.covertor.OperationLogParamValueConverter;
import org.shoulder.log.operation.covertor.OperationLogParamValueConverterHolder;
import org.shoulder.log.operation.covertor.DefaultOperationLogParamValueConverter;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志参数转换器
 *
 * @author lym
 */
@Configuration
@EnableConfigurationProperties(OperationLogProperties.class)
public class OperationLogParamConverterAutoConfiguration {


    /**
     * 默认的 value 解析器
     */
    @Bean
    public DefaultOperationLogParamValueConverter defaultOperationLogParamValueConverter(OperationLogProperties OperationLogProperties){

        return new DefaultOperationLogParamValueConverter(OperationLogProperties.getNullParamOutput());
    }

    /**
     * 收集所有 converter
     */
    @Bean
    public OperationLogParamValueConverterHolder OperationLogParamValueConverterHolder(
            ListableBeanFactory beanFactory,
            DefaultOperationLogParamValueConverter defaultConverter
    ){

        Map<String, OperationLogParamValueConverter> allConverterMap =
                beanFactory.getBeansOfType(OperationLogParamValueConverter.class);

        boolean hasConverter = MapUtils.isNotEmpty(allConverterMap);

        OperationLogParamValueConverterHolder holder =
                new OperationLogParamValueConverterHolder(
                        hasConverter ?
                                new HashMap<>(allConverterMap.size())
                                : Collections.emptyMap(),
                defaultConverter);

        if(hasConverter){
            allConverterMap.values().forEach(holder::addConverter);
        }

        return holder;

    }


}
