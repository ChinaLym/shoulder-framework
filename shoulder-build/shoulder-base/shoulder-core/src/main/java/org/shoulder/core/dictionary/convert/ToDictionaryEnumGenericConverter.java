package org.shoulder.core.dictionary.convert;

import org.shoulder.core.dictionary.model.DictionaryItem;
import org.shoulder.core.dictionary.model.DictionaryItemEnum;
import org.shoulder.core.util.StringUtils;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.lang.NonNull;

import java.util.Set;

/**
 * Int/String -> Enum
 *
 * @author lym
 */
@SuppressWarnings("unchecked, rawtypes")
public class ToDictionaryEnumGenericConverter implements ConditionalGenericConverter {

    public static final ToDictionaryEnumGenericConverter INSTANCE = new ToDictionaryEnumGenericConverter();

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(new ConvertiblePair(String.class, DictionaryItem.class),
                new ConvertiblePair(Integer.class, DictionaryItem.class));
    }

    @Override
    public boolean matches(@NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
        return targetType.getType().isEnum();
    }

    @Override
    public Object convert(Object source, @NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        Class<?> sourceIntStringClass = sourceType.getType();
        if (sourceIntStringClass == Integer.class) {
            return parseInt2Enum((Integer) source, targetType.getType());
        } else if (sourceType.getType() == String.class) {
            return parseStr2Enum((String) source, targetType.getType());
        }
        throw new IllegalStateException("cannot reachable");
    }

    public static Enum<? extends DictionaryItemEnum> parseStr2Enum(String sourceString, Class<?> targetEnumClass) {
        // String -> Enum
        Class<?> identifyType = DictionaryItemEnum.resovleEnumItemIdClass(targetEnumClass);
        Class<? extends Enum<? extends DictionaryItemEnum<?, String>>> enumClass = (Class<? extends Enum<? extends DictionaryItemEnum<?, String>>>) targetEnumClass;
        if (identifyType == String.class) {

            // 1. fromId
            return DictionaryItemEnum.decideActualEnum(enumClass.getEnumConstants(), sourceString, DictionaryItemEnum.compareWithId(),
                    // 2. from name with
                    (enumCls, sourceStr) -> parseStrToIntEnum(sourceString, targetEnumClass));
        }
        return parseStrToIntEnum(sourceString, targetEnumClass);
    }

    public static Enum<? extends DictionaryItemEnum<?, String>> parseStrToIntEnum(String sourceString, Class<?> targetEnumClass) {
        Class<? extends Enum<? extends DictionaryItemEnum<?, String>>> enumClass = (Class<? extends Enum<? extends DictionaryItemEnum<?, String>>>) targetEnumClass;
        return DictionaryItemEnum.decideActualEnum((enumClass).getEnumConstants(), sourceString, DictionaryItemEnum.compareWithEnumCodingName(),
                (enumCls2, sourceStr2) -> {
                    // 3. 兜底判断是否为数字，尝试用数字转换
                    if (StringUtils.isNumeric((String) sourceStr2)) {
                        int intVal = Integer.parseInt((String) sourceStr2);
                        return (Enum<? extends DictionaryItemEnum<?, String>>) parseInt2Enum(intVal, targetEnumClass);
                    }
                    // 找不到，肯定输入和当前代码版本不一致且这种使用方式无法兼容，报错
                    return DictionaryItemEnum.onMissMatch(enumCls2, sourceStr2);
                });
    }

    public static Object parseInt2Enum(Integer sourceInteger, Class<?> targetEnumClass) {
        // int -> Enum
        Class<?> identifyType = DictionaryItemEnum.resovleEnumItemIdClass(targetEnumClass);
        if (identifyType == Integer.class) {
            // 1. fromId
            return DictionaryItemEnum.fromId((Class<? extends Enum<? extends DictionaryItemEnum<?, Integer>>>) targetEnumClass, sourceInteger);
        }

        // 2. from index
        Object[] enumItems = targetEnumClass.getEnumConstants();
        if (sourceInteger >= 0 && sourceInteger < enumItems.length) {
            return enumItems[sourceInteger];
        } else {
            // out of index
            throw new IllegalArgumentException("cannot convert [" + sourceInteger + "] To [" + targetEnumClass + "]");
        }
    }
}
