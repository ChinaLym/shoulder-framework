package org.shoulder.core.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 枚举转换工厂
 * <p>
 * 需要在WebMvcConfigurer 实现类中注册该类
 *
 * @author lym
 */
public class EnumConverterFactory implements ConverterFactory<String, Enum<? extends Enum<?>>> {

    private final EnumMissMatchHandler missMatchHandler;

    private static final ConcurrentMap<Class<? extends Enum<?>>, Converter<String, ? extends Enum<?>>>
            CONVERTER_CACHE = new ConcurrentHashMap<>();

    public EnumConverterFactory(EnumMissMatchHandler missMatchHandler) {
        this.missMatchHandler = missMatchHandler;
    }

    public static EnumConverterFactory getDefaultInstance() {
        return EnumConverterFactory.EnumConverterFactoryHolder.INSTANCE;
    }

    static class EnumConverterFactoryHolder {
        private static final EnumConverterFactory INSTANCE =
                new EnumConverterFactory(DefaultEnumMissMatchHandler.getInstance());
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nonnull
    public <T extends Enum<?>> Converter<String, T> getConverter(@Nonnull Class<T> targetType) {
        return (Converter<String, T>) CONVERTER_CACHE.computeIfAbsent(targetType,
                t -> new EnumConverter(t, missMatchHandler));
    }



}
