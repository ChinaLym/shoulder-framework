package org.shoulder.core.util;

import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.boot.autoconfigure.web.format.WebConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.ConfigurableConversionService;

import javax.annotation.Nullable;

/**
 * 类型转换
 *
 * @author lym
 */
public class ConvertUtil {

    private static final String ISO_DATE_FORMAT = "iso";

    private static final ConfigurableConversionService CONVERSION_SERVICE = new WebConversionService(
            new DateTimeFormatters()
                    .dateFormat(ISO_DATE_FORMAT)
                    .timeFormat(ISO_DATE_FORMAT)
                    .dateTimeFormat(ISO_DATE_FORMAT)
    );

    public static boolean canConvert(@Nullable Class<?> source, Class<?> target) {
        return CONVERSION_SERVICE.canConvert(source, target);
    }

    public static boolean canConvert(@Nullable TypeDescriptor source, TypeDescriptor target) {
        return CONVERSION_SERVICE.canConvert(source, target);
    }

    @Nullable
    public static <T> T convert(@Nullable Object source, Class<T> target) {
        return CONVERSION_SERVICE.convert(source, target);
    }

    @Nullable
    public static Object convert(@Nullable Object source, @Nullable TypeDescriptor sourceType, TypeDescriptor targetType) {
        return CONVERSION_SERVICE.convert(source, sourceType, targetType);
    }

    public static void addConverter(Converter<?, ?> converter) {
        CONVERSION_SERVICE.addConverter(converter);
    }

    public static <S, T> void addConverter(Class<S> sourceType, Class<T> targetType, Converter<? super S, ? extends T> converter) {
        CONVERSION_SERVICE.addConverter(sourceType, targetType, converter);
    }

    public static void addConverter(GenericConverter converter) {
        CONVERSION_SERVICE.addConverter(converter);
    }

    public static void addConverterFactory(ConverterFactory<?, ?> factory) {
        CONVERSION_SERVICE.addConverterFactory(factory);
    }

    public static void removeConvertible(Class<?> sourceType, Class<?> targetType) {
        CONVERSION_SERVICE.removeConvertible(sourceType, targetType);
    }


}