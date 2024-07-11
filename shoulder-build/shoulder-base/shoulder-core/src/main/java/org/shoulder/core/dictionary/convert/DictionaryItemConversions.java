package org.shoulder.core.dictionary.convert;

import org.shoulder.core.dictionary.model.DictionaryItem;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

/**
 * str / DictionaryItem<String> 转换，方便 JPA、Mongodb 等扩展使用
 *
 * @author lym
 */
public class DictionaryItemConversions {
    public static DictionaryItem<?> toItem(Object value, Class<?> actuallyType) {
        return (DictionaryItem<?>) ToDictionaryEnumGenericConverter.INSTANCE.convert(value, TypeDescriptor.valueOf(String.class),
            TypeDescriptor.valueOf(actuallyType));
    }

    public static String toStr(DictionaryItem<?> value) {
        if (value == null) {
            return null;
        }
        Class<?> actuallyType = value.getClass();
        GenericConverter converter = actuallyType.isEnum()
            ? DictionaryItemEnumSerialGenericConverter.INSTANCE
            : DictionaryItemToStrGenericConverter.INSTANCE;
        return (String) converter.convert(value, TypeDescriptor.valueOf(actuallyType), TypeDescriptor.valueOf(String.class));
    }

}
