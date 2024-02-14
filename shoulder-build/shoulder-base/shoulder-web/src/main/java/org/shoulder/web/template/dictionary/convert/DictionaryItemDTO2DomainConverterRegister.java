package org.shoulder.web.template.dictionary.convert;

import org.shoulder.core.converter.ShoulderConversionService;
import org.shoulder.core.dictionary.convert.DictionaryItemEnumSerialGenericConverter;
import org.shoulder.core.dictionary.convert.DictionaryItemToStrGenericConverter;
import org.shoulder.core.dictionary.convert.ToDictionaryEnumGenericConverter;
import org.shoulder.core.dictionary.model.DictionaryItemEnum;
import org.shoulder.core.dictionary.model.DictionaryType;
import org.shoulder.core.dictionary.spi.DictionaryEnumStore;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Collection;

/**
 * 专门为 dictionary 实现的通用转换逻辑，这样代码少，更容易JIT编译加速
 * <p>
 * 所有需要转换的类型：
 * 非业务逻辑层：DictionaryItemDTO、Integer、String
 * 业务逻辑层：DictionaryItem、DictionaryItemEntity、ConfigAbleDictionaryItem
 * <p>
 * 转为core模型（DTO/PO -> Core.BizModel）
 * DictionaryItemDTO -> Enum、DictionaryItemEntity、ConfigAbleDictionaryItem
 * Integer/String -> Enum
 * <p>
 * 转为外部类型 (Core.BizModel -> DTO/PO)
 * Enum -> Integer/String
 * DictionaryItem -> String
 * DictionaryItem -> DictionaryItemDTO
 * <p>
 * 其他（model.of 已实现）
 * DictionaryItem -> DictionaryItemEntity、ConfigAbleDictionaryItem
 *
 * @author lym
 */
@SuppressWarnings("rawtypes")
public class DictionaryItemDTO2DomainConverterRegister {

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshedEvent(ContextRefreshedEvent event) {
        ShoulderConversionService conversionService = event.getApplicationContext().getBean(ShoulderConversionService.class);

        // ======= 字典类型 ======
        conversionService.addConverter(DictionaryType.class, String.class, DictionaryType::getCode);

        // ======= 字典项 ====== 枚举字典
        Collection<Class<? extends Enum<? extends DictionaryItemEnum>>> enumClassList = event.getApplicationContext().getBean(
                        DictionaryEnumStore.class)
                .listAllTypes();

        // dto -> Enum、DictionaryItemEntity、ConfigAbleDictionaryItem
        conversionService.addConverter(DictionaryDTO2DictionaryItemGenericConverter.INSTANCE);
        // integer/string -> Enum
        conversionService.addConverter(ToDictionaryEnumGenericConverter.INSTANCE);
        // Core.Enum -> integer/string
        conversionService.addConverter(DictionaryItemEnumSerialGenericConverter.INSTANCE);
        // Core.DictionaryItem(!Enum) -> String
        conversionService.addConverter(DictionaryItemToStrGenericConverter.INSTANCE);
        // core.DictionaryItem -> dto
        conversionService.addConverter(event.getApplicationContext().getBean(DictionaryItemDomain2DTOConverter.class));
    }

}
