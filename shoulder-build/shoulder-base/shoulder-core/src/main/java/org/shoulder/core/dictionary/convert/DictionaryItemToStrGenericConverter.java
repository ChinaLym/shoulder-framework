package org.shoulder.core.dictionary.convert;

import org.shoulder.core.dictionary.model.DictionaryItem;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.lang.NonNull;

import java.util.Set;

/**
 * Core.Dictionary -> String
 *
 * @author lym
 */
@SuppressWarnings("unchecked")
public class DictionaryItemToStrGenericConverter implements ConditionalGenericConverter {

    public static final DictionaryItemToStrGenericConverter INSTANCE = new DictionaryItemToStrGenericConverter();

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(new ConvertiblePair(DictionaryItem.class, String.class));
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
        return !sourceType.getType().isEnum();
    }

    @Override
    public Object convert(Object source, @NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        return ((DictionaryItem<String>) source).getItemId();
    }
}
