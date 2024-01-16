package org.shoulder.web.template.dictionary.base;

import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDTO;
import org.shoulder.web.template.dictionary.model.DictionaryEnum;
import org.shoulder.web.template.dictionary.spi.DictionaryEnumStore;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 专门为 dictionary 实现的通用转换逻辑，这样代码少，更容易JIT编译加速
 * 支持 int/string 到 DictionaryEnum 的转换
 * DictionaryItemDTO 到 DictionaryEnum 的转换
 *
 * @author lym
 */
public class ToDictionaryEnumConversionService extends GenericConversionService {

    private DictionaryEnumStore dictionaryEnumStore;

    @Override
    public boolean canConvert(@Nullable TypeDescriptor sourceType, TypeDescriptor targetType) {
        Assert.notNull(targetType, "Target type to convert to cannot be null");
        return sourceType == null ||
               (DictionaryEnum.class.isAssignableFrom(targetType.getType()) &&
                                   (sourceType.getType() == Integer.class || sourceType.getType() == String.class || sourceType.getType() == DictionaryItemDTO.class));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override public Object convert(@Nullable Object source, @Nullable TypeDescriptor sourceType, TypeDescriptor targetType) {

        if (source == null) {
            return null;
        }
        if (sourceType.getType() == Integer.class) {
            return DictionaryEnum.fromId((Class<? extends Enum<? extends DictionaryEnum<?, Integer>>>) targetType.getType(), (Integer) source);
        }
        if (sourceType.getObjectType() == String.class) {
            return DictionaryEnum.fromId((Class<? extends Enum<? extends DictionaryEnum<?, String>>>) targetType.getType(), (String) source);
        }
        if (sourceType.getObjectType() == DictionaryItemDTO.class) {
            String code = ((DictionaryItemDTO)source).getCode();
            //Class<? extends Enum<? extends DictionaryEnum>> dictionaryEnum = dictionaryEnumStore.getActuallyType(type);
            return DictionaryEnum.fromId((Class<? extends Enum<? extends DictionaryEnum<?, String>>>) targetType.getType(), code);
        }
        throw new BaseRuntimeException("unreachable");
    }
}
