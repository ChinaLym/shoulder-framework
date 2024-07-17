package org.shoulder.web.template.dictionary.convert;

import org.shoulder.core.dictionary.convert.DictionaryItemEnumSerialGenericConverter;
import org.shoulder.core.dictionary.convert.DictionaryItemToStrGenericConverter;
import org.shoulder.core.dictionary.convert.ToDictionaryEnumGenericConverter;
import org.shoulder.core.dictionary.model.DictionaryItem;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.data.convert.PropertyValueConverter;
import org.springframework.data.convert.ValueConversionContext;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.lang.NonNull;

/**
 * db / mongodb entity.field(DictionaryItemEnum) convert to str
 * spring-data 属性转换接口，方便在 java entity 代码用枚举，数据库里存字符串
 *
 * @author lym
 * @see PackageScanDictionaryItem2StrConverterMongoConfiguration 如 mongodb 配置该类，将在读写 mongodb 时候自动转换其类型，而无需手动标记 @ReadConverter 等
 */
public class EnumToStringPropertyValueConverter
        implements PropertyValueConverter<DictionaryItem<String>, String, ValueConversionContext<? extends PersistentProperty>> {

    @SuppressWarnings("unchecked")
    @Override
    public DictionaryItem<String> read(@NonNull String value, ValueConversionContext<? extends PersistentProperty> context) {
        Class<?> actuallyType = context.getProperty().getActualType();
        return (DictionaryItem<String>) ToDictionaryEnumGenericConverter.INSTANCE.convert(value, TypeDescriptor.valueOf(String.class),
                TypeDescriptor.valueOf(actuallyType));
    }

    @Override
    public String write(@NonNull DictionaryItem<String> value, ValueConversionContext<? extends PersistentProperty> context) {
        Class<?> actuallyType = context.getProperty().getActualType();
        GenericConverter converter = actuallyType.isEnum()
                ? DictionaryItemEnumSerialGenericConverter.INSTANCE
                : DictionaryItemToStrGenericConverter.INSTANCE;
        return (String) converter.convert(value, TypeDescriptor.valueOf(actuallyType), TypeDescriptor.valueOf(String.class));
    }

}
