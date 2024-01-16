package org.shoulder.web.template.dictionary.base;

import jakarta.annotation.Nonnull;
import org.shoulder.core.converter.ShoulderConversionService;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDTO;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDTO2ConfigAbleDictionaryItemConverter;
import org.shoulder.web.template.dictionary.model.ConfigAbleDictionaryItem;
import org.shoulder.web.template.dictionary.model.DictionaryEnum;
import org.shoulder.web.template.dictionary.model.DictionaryItem;
import org.shoulder.web.template.dictionary.model.DictionaryItemEntity;
import org.shoulder.web.template.dictionary.spi.DictionaryEnumStore;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;

import java.util.Collection;

/**
 * DictionaryItemDTO -> domain
 *
 * 所有需要转换的类型：
 * 非业务逻辑层：DictionaryItemDTO、Integer、String
 * 业务逻辑层：DictionaryItem、DictionaryItemEntity、ConfigAbleDictionaryItem
 *
 * 转为core模型（Controller、Repository To BizModel）
 * DictionaryItemDTO -> Enum
 * DictionaryItemDTO -> DictionaryItemEntity
 * DictionaryItemDTO -> ConfigAbleDictionaryItem
 * Integer -> Enum
 * String -> Enum
 * 转为外部类型（BizModel To Controller、Repository）
 * DictionaryItem -> DictionaryItemDTO
 * DictionaryItem -> Integer
 * DictionaryItem -> String
 * 不常用的
 * Enum -> DictionaryItemEntity
 * Enum -> ConfigAbleDictionaryItem
 *
 * @author lym
 */
@SuppressWarnings("unchecked, rawtypes")
public class DictionaryItemDTO2DomainConverterRegister {

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshedEvent(ContextRefreshedEvent event) {
        ShoulderConversionService conversionService = event.getApplicationContext().getBean(ShoulderConversionService.class);
        // 枚举字典
        Collection<Class<? extends Enum<? extends DictionaryEnum>>> enumClassList = event.getApplicationContext().getBean(DictionaryEnumStore.class)
                .listAllTypes();

        // enum/dynamic 2 dto
//        conversionService.addConverter(DictionaryItem.class, DictionaryItemDTO.class, source -> {
//            DictionaryItemDTO dto = new DictionaryItemDTO();
//            dto.setCode(source.getItemId().toString());
//            dto.setDictionaryType(source.getDictionaryType());
//            dto.setDisplayName(source.getDisplayName());
//            dto.setDisplayOrder(source.getDisplayOrder());
//            //dto.setParentCode();
//            return dto;
//        });

        // todo integer/string special

        // dto -> enum
        for (Class<? extends Enum<? extends DictionaryEnum>> enumClass : enumClassList) {
            DictionaryItemDTO2DomainConverter converter = new DictionaryItemDTO2DomainConverter(enumClass);
            conversionService.addConverter(DictionaryItemDTO.class, enumClass, converter);
        }
        // dto -> 基于动态配置的 model
        conversionService.addConverter(DictionaryItemDTO.class, ConfigAbleDictionaryItem.class, new DictionaryItemDTO2ConfigAbleDictionaryItemConverter());
        // dto -> 基于存储的 model
        conversionService.addConverter(DictionaryItemDTO.class, DictionaryItemEntity.class, dto -> {
            DictionaryItemEntity entity = new DictionaryItemEntity<>();
            entity.setDictionaryId(dto.getDictionaryType());
            entity.setBizId(dto.getCode());
            entity.setName(dto.getName());
            entity.setDisplayName(dto.getDisplayName());
            entity.setSortNo(dto.getDisplayOrder());
            entity.setNote(dto.getNote());
            return entity;
        });
    }

    public static class DictionaryItemDTO2DomainConverter<T extends DictionaryItem> implements Converter<DictionaryItemDTO, T> {

        private final Class targetClass;

        public DictionaryItemDTO2DomainConverter(Class<T> targetClass) {
            this.targetClass = targetClass;
        }

        @Override
        public T convert(@Nonnull DictionaryItemDTO sourceModel) {
            return (T) DictionaryEnum.fromId(targetClass, sourceModel.getCode());
        }
    }
}
