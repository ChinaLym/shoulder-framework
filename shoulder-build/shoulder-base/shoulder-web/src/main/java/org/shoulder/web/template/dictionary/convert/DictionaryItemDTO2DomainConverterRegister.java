package org.shoulder.web.template.dictionary.convert;

import org.shoulder.core.converter.ShoulderConversionService;
import org.shoulder.core.util.StringUtils;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDTO;
import org.shoulder.web.template.dictionary.model.ConfigAbleDictionaryItem;
import org.shoulder.web.template.dictionary.model.DictionaryEnum;
import org.shoulder.web.template.dictionary.model.DictionaryItem;
import org.shoulder.web.template.dictionary.model.DictionaryItemEntity;
import org.shoulder.web.template.dictionary.spi.DictionaryEnumStore;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * 专门为 dictionary 实现的通用转换逻辑，这样代码少，更容易JIT编译加速
 *
 * 所有需要转换的类型：
 * 非业务逻辑层：DictionaryItemDTO、Integer、String
 * 业务逻辑层：DictionaryItem、DictionaryItemEntity、ConfigAbleDictionaryItem
 *
 * 转为core模型（DTO/PO -> Core.BizModel）
 * DictionaryItemDTO -> Enum、DictionaryItemEntity、ConfigAbleDictionaryItem
 * Integer/String -> Enum
 *
 * 转为外部类型 (Core.BizModel -> DTO/PO)
 * Enum -> Integer/String
 * DictionaryItem -> String
 * DictionaryItem -> DictionaryItemDTO
 *
 * 其他（model.of 已实现）
 * DictionaryItem -> DictionaryItemEntity、ConfigAbleDictionaryItem
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
        Collection<Class<? extends Enum<? extends DictionaryEnum>>> enumClassList = event.getApplicationContext().getBean(
                DictionaryEnumStore.class)
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

        // dto -> Enum、DictionaryItemEntity、ConfigAbleDictionaryItem
        conversionService.addConverter(new DictionaryDTO2DictionaryItemGenericConverter());
        // integer/string -> Enum
        conversionService.addConverter(new ToDictionaryEnumGenericConverter());
        // Core.Enum -> integer/string
        conversionService.addConverter(new DictionaryEnumSerialGenericConverter());
        // Core.DictionaryItem(!Enum) -> String
        conversionService.addConverter(new DictionaryItemToStrGenericConverter());
        // core.DictionaryItem -> dto
        conversionService.addConverter(event.getApplicationContext().getBean(DictionaryItemDomain2DTOConverter.class));
    }

    /**
     * DictionaryItemDTO -> Enum、DictionaryItemEntity、ConfigAbleDictionaryItem
     */
    public static class DictionaryDTO2DictionaryItemGenericConverter implements GenericConverter {

        @Override public Set<ConvertiblePair> getConvertibleTypes() {
            return Set.of(new ConvertiblePair(DictionaryItemDTO.class, DictionaryItem.class));
        }

        @Override public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
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

    /**
     * Int/String -> Enum
     */
    public static class ToDictionaryEnumGenericConverter implements ConditionalGenericConverter {
        @Override public Set<ConvertiblePair> getConvertibleTypes() {
            return Set.of(new ConvertiblePair(String.class, DictionaryItem.class),
                new ConvertiblePair(Integer.class, DictionaryItem.class));
        }

        @Override public boolean matches(@NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
            return targetType.getType().isEnum();
        }


        @Override public Object convert(Object source, @NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
            if (source == null) {
                return null;
            }
            Class<?> sourceIntStringClass = sourceType.getType();
            if (sourceIntStringClass == Integer.class) {
                return parseInt2Enum((Integer) source, targetType.getType());
            } else if (sourceType.getObjectType() == String.class) {
                return parseStr2Enum((String) source, targetType.getType());
            }
            throw new IllegalStateException("cannot reachable");
        }

        private static Enum<? extends DictionaryEnum> parseStr2Enum(String source, Class<?> targetClass) {
            // String -> Enum
            Class<?> identifyType = Optional.ofNullable(GenericTypeResolver.resolveTypeArguments(targetClass, DictionaryEnum.class)).orElseThrow()[0];
            if (identifyType == String.class) {
                // 1. fromId
                return DictionaryEnum.fromId((Class<? extends Enum<? extends DictionaryEnum<?, String>>>) targetClass,
                    source);
            }
            // 2. from name with onMissMatch 尝试转为数字识别
            Class<? extends Enum<? extends DictionaryEnum<?, String>>> enumClass = (Class<? extends Enum<? extends DictionaryEnum<?, String>>>) targetClass;
            return DictionaryEnum.fromNameWithEnumConstants(enumClass.getEnumConstants(), source, (enumCls, sourceStr) -> {
                if(StringUtils.isNumeric((String) sourceStr)) {
                    int intVal = Integer.parseInt((String) sourceStr);
                    return (Enum<? extends DictionaryEnum<?, String>>) parseInt2Enum(intVal, targetClass);
                }
                return DictionaryEnum.onMissMatch(enumCls, sourceStr);
            });
        }

        public static Object parseInt2Enum(Integer source, Class<?> targetClass) {
            // int -> Enum
            Class<?> identifyType = Optional.ofNullable(GenericTypeResolver.resolveTypeArguments(targetClass, DictionaryEnum.class)).orElseThrow()[0];
            if (identifyType == Integer.class) {
                // 1. fromId
                return DictionaryEnum.fromId((Class<? extends Enum<? extends DictionaryEnum<?, Integer>>>) targetClass, source);
            }

            // 2. from index
            Object[] enumItems = targetClass.getEnumConstants();
            if (source >= 0 && source < enumItems.length) {
                return enumItems[source];
            } else {
                // out of index
                throw new IllegalArgumentException("cannot convert [" + source + "] To [" + targetClass + "]");
            }
        }
    }


    /**
     * Core.Dictionary -> String
     */
    public static class DictionaryItemToStrGenericConverter implements ConditionalGenericConverter {
        @Override public Set<ConvertiblePair> getConvertibleTypes() {
            return Set.of(new ConvertiblePair(DictionaryItem.class, String.class));
        }

        @Override public boolean matches(TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
            return !sourceType.getType().isEnum();
        }

        @Override public Object convert(Object source, @NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
            if (source == null) {
                return null;
            }
            return ((DictionaryItem<String>)source).getItemId();
        }
    }

    /**
     * Enum -> Int/String
     */
    public static class DictionaryEnumSerialGenericConverter implements ConditionalGenericConverter {
        @Override public Set<ConvertiblePair> getConvertibleTypes() {
            return Set.of(new ConvertiblePair(DictionaryItem.class, String.class),
                new ConvertiblePair(DictionaryItem.class, Integer.class));
        }

        @Override public boolean matches(TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
            return sourceType.getType().isEnum();
        }

        @Override public Object convert(Object source, @NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
            if (source == null) {
                return null;
            }
            Class<?> sourceIntStringClass = sourceType.getType();
            Class<?> targetEnumClass = targetType.getType();
            Class<?> identifyType = Optional.ofNullable(GenericTypeResolver.resolveTypeArguments(targetEnumClass, DictionaryEnum.class)).orElseThrow()[0];
            if (sourceIntStringClass == Integer.class) {
                // toInt
                if (identifyType == Integer.class) {
                    // 1. id
                    return ((DictionaryItem<Integer>)source).getItemId();
                }
                // 2. index
                return ((Enum<?>)source).ordinal();
            } else if (sourceType.getObjectType() == String.class) {
                // toString
                if (identifyType == String.class) {
                    // 1. id
                    return ((DictionaryItem<String>)source).getItemId();
                }
                // 2. enumName
                return ((Enum<?>)source).name();
            }
            throw new IllegalStateException("cannot reachable");
        }
    }
}
