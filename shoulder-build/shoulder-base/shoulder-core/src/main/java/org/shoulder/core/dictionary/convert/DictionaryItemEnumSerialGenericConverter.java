package org.shoulder.core.dictionary.convert;

import org.shoulder.core.dictionary.model.DictionaryItem;
import org.shoulder.core.dictionary.model.DictionaryItemEnum;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.lang.NonNull;

import java.util.Set;

/**
 * Enum -> Int/String
 *
 * @author lym
 */
@SuppressWarnings("unchecked")
public class DictionaryItemEnumSerialGenericConverter implements ConditionalGenericConverter {

    public static final DictionaryItemEnumSerialGenericConverter INSTANCE = new DictionaryItemEnumSerialGenericConverter();

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(new ConvertiblePair(DictionaryItem.class, String.class),
                new ConvertiblePair(DictionaryItem.class, Integer.class));
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
        return sourceType.getType().isEnum()
               && (targetType.getType().isAssignableFrom(String.class) || targetType.getType().isAssignableFrom(Integer.class));
    }

    @Override
    public Object convert(Object source, @NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        Class<?> sourceEnumClass = sourceType.getType();
        Class<?> targetIntStringClass = targetType.getType();
        Class<?> identifyType = DictionaryItemEnum.resovleEnumItemIdClass(sourceEnumClass);
        if (targetIntStringClass == Integer.class) {
            // toInt
            if (identifyType == Integer.class) {
                // 1. id
                return ((DictionaryItem<Integer>) source).getItemId();
            }
            // 2. index
            return ((Enum<?>) source).ordinal();
        } else if (targetIntStringClass == String.class) {
            // toString
            if (identifyType == String.class) {
                // 1. id
                return ((DictionaryItem<String>) source).getItemId();
            }
            // 2. enumName
            return ((Enum<?>) source).name();
        }
        throw new IllegalStateException("cannot reachable");
    }
}
