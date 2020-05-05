package org.shoulder.log.operation.covertor;

import java.util.Map;

/**
 * 转换器存储
 *
 * @author lym
 */
public class OperationLogParamValueConverterHolder {

    private Map<Class<? extends OperationLogParamValueConverter>, OperationLogParamValueConverter> converterMap;

    private OperationLogParamValueConverter defaultConvert;

    public OperationLogParamValueConverterHolder(Map<Class<? extends OperationLogParamValueConverter>,
            OperationLogParamValueConverter> converterMap, OperationLogParamValueConverter defaultConvert) {
        this.converterMap = converterMap;
        this.defaultConvert = defaultConvert;
    }

    public OperationLogParamValueConverter getConvert(Class<? extends OperationLogParamValueConverter> convertClazz) {
        return converterMap.getOrDefault(convertClazz, defaultConvert);
    }

    public void addConverter(OperationLogParamValueConverter converter) {
        if (converter == null) {
            return;
        }
        converterMap.put(converter.getClass(), converter);
    }

}