package org.shoulder.core.converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import javax.annotation.Nonnull;

/**
 * 枚举转换工厂
 * <p>
 * 需要在WebMvcConfigurer 实现类中注册该类
 *
 * @author lym
 */
public class EnumConverterFactory implements ConverterFactory<String, Enum> {

    @Autowired
    private EnumMissMatchHandler missMatchHandler = new DefaultEnumMissMatchHandler();

    @SuppressWarnings("unchecked")
    @Override
    @Nonnull
    public <T extends Enum> Converter<String, T> getConverter(@Nonnull Class<T> targetType) {
        return (Converter<String, T>) new EnumConverter(targetType, missMatchHandler);
    }
}
