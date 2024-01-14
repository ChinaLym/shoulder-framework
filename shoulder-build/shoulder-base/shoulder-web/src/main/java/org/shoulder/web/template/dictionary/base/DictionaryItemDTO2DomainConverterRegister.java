package org.shoulder.web.template.dictionary.base;

import jakarta.annotation.Nonnull;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDTO;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDTO2ConfigAbleDictionaryItemConverter;
import org.shoulder.web.template.dictionary.model.ConfigAbleDictionaryItem;
import org.shoulder.web.template.dictionary.model.DictionaryEnum;
import org.shoulder.web.template.dictionary.model.DictionaryItem;
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

        for (Class<? extends Enum<? extends DictionaryEnum>> enumClass : enumClassList) {
            DictionaryItemDTO2DomainConverter converter = new DictionaryItemDTO2DomainConverter(enumClass);
            conversionService.addConverter(DictionaryItemDTO.class, enumClass, converter);
        }
        // 动态配置字典
        conversionService.addConverter(DictionaryItemDTO.class, ConfigAbleDictionaryItem.class, new DictionaryItemDTO2ConfigAbleDictionaryItemConverter());
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
