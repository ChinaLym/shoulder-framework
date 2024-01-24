package org.shoulder.web.template.dictionary.convert;

import org.shoulder.web.template.dictionary.model.DictionaryItem;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

public class DictionaryItemConversions {
    public static DictionaryItem<String> toItem(Object value, Class<?> actuallyType) {
        return (DictionaryItem<String>) ToDictionaryEnumGenericConverter.INSTANCE.convert(value, TypeDescriptor.valueOf(String.class),
                TypeDescriptor.valueOf(actuallyType));
    }

    public static String toStr(DictionaryItem<String> value) {
        if (value == null) {
            return null;
        }
        Class<?> actuallyType = value.getClass();
        GenericConverter converter = actuallyType.isEnum()
                ? DictionaryEnumSerialGenericConverter.INSTANCE
                : DictionaryItemToStrGenericConverter.INSTANCE;
        return (String) converter.convert(value, TypeDescriptor.valueOf(actuallyType), TypeDescriptor.valueOf(String.class));
    }

}
