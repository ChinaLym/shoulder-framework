package org.shoulder.web.template.dictionary.convert;

import org.shoulder.web.template.dictionary.dto.DictionaryItemDTO;
import org.shoulder.web.template.dictionary.model.ConfigAbleDictionaryItem;
import org.shoulder.web.template.dictionary.model.DictionaryItem;
import org.shoulder.web.template.dictionary.model.DictionaryItemEntity;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import java.util.Set;

/**
 * DictionaryItemDTO -> Enum、DictionaryItemEntity、ConfigAbleDictionaryItem
 *
 * @author lym
 */
@SuppressWarnings("unchecked, rawtypes")
public class DictionaryDTO2DictionaryItemGenericConverter implements GenericConverter {

    public static final DictionaryDTO2DictionaryItemGenericConverter INSTANCE = new DictionaryDTO2DictionaryItemGenericConverter();

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(new ConvertiblePair(DictionaryItemDTO.class, DictionaryItem.class));
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        DictionaryItemDTO dto = (DictionaryItemDTO) source;
        Class<?> targetClass = targetType.getType();
        if (targetClass.isEnum()) {
            // dto -> Enum (str -> enum)
            return ToDictionaryEnumGenericConverter.parseStr2Enum(dto.getCode(), targetClass);

        } else if (targetClass == ConfigAbleDictionaryItem.class) {
            // dto -> 基于动态配置的 model
            ConfigAbleDictionaryItem targetModel = new ConfigAbleDictionaryItem();
            targetModel.setCode(dto.getCode());
            // 为空或者为0，都代表一级节点
            //        targetModel.setParentCode(StringUtils.isBlank(dto.getParentCode()) ? Constants.ZERO : dto
            //        .getParentCode());
            targetModel.setDictionaryType(dto.getDictionaryType());
            targetModel.setName(targetModel.getName());
            targetModel.setDisplayName(dto.getDisplayName());
            targetModel.setDisplayOrder(dto.getDisplayOrder());
            targetModel.setNote(dto.getNote());
            return targetModel;
        } else if (targetClass == DictionaryItemEntity.class) {
            // dto -> 基于存储的 model
            DictionaryItemEntity entity = new DictionaryItemEntity<>();
            // to confirm dictionaryType.code
            entity.setDictionaryId(dto.getDictionaryType());
            entity.setBizId(dto.getCode());
            entity.setName(dto.getName());
            entity.setDisplayName(dto.getDisplayName());
            entity.setSortNo(dto.getDisplayOrder());
            entity.setNote(dto.getNote());
            return entity;
        }
        throw new IllegalStateException("cannot reachable");
    }

}
