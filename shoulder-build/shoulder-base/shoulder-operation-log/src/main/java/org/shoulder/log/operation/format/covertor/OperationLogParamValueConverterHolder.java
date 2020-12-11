package org.shoulder.log.operation.format.covertor;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.log.operation.format.OperationLogParamValueConverter;

import javax.annotation.Nonnull;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 转换器存储
 *
 * @author lym
 */
public class OperationLogParamValueConverterHolder {

    private static Map<Class<? extends OperationLogParamValueConverter>, OperationLogParamValueConverter> converterMap;

    private static OperationLogParamValueConverter defaultConvert;

    @Nonnull
    public static OperationLogParamValueConverter getConvert(Class<? extends OperationLogParamValueConverter> convertClazz) {
        return converterMap.getOrDefault(convertClazz, defaultConvert);
    }

    /**
     * 初始化
     *
     * @param converterList  所有的参数转换器
     * @param defaultConvert 默认的转换器
     */
    public static void init(@Nonnull Collection<OperationLogParamValueConverter> converterList, OperationLogParamValueConverter defaultConvert) {
        boolean hasConverter = CollectionUtils.isNotEmpty(converterList);
        converterMap = new HashMap<>(converterList.size());

        if (hasConverter) {
            converterList.forEach(OperationLogParamValueConverterHolder::addConverter);
        }
        if (defaultConvert == null) {
            throw new IllegalArgumentException("defaultConvert can't be null!");
        }
        OperationLogParamValueConverterHolder.defaultConvert = defaultConvert;
    }

    private static void addConverter(OperationLogParamValueConverter converter) {
        if (converter == null) {
            throw new InvalidParameterException("param converter can't be null");
        }
        converterMap.put(converter.getClass(), converter);
    }


}
