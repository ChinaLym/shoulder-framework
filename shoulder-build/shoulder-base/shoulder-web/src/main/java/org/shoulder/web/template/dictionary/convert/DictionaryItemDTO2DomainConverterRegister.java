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
        conversionService.addConverter(DictionaryDTO2DictionaryItemGenericConverter.INSTANCE);
        // integer/string -> Enum
        conversionService.addConverter(ToDictionaryEnumGenericConverter.INSTANCE);
        // Core.Enum -> integer/string
        conversionService.addConverter(DictionaryEnumSerialGenericConverter.INSTANCE);
        // Core.DictionaryItem(!Enum) -> String
        conversionService.addConverter(DictionaryItemToStrGenericConverter.INSTANCE);
        // core.DictionaryItem -> dto
        conversionService.addConverter(event.getApplicationContext().getBean(DictionaryItemDomain2DTOConverter.class));
    }

    /**
     * DictionaryItemDTO -> Enum、DictionaryItemEntity、ConfigAbleDictionaryItem
     */
    public static class DictionaryDTO2DictionaryItemGenericConverter implements GenericConverter {

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

    /**
     * Int/String -> Enum
     */
    public static class ToDictionaryEnumGenericConverter implements ConditionalGenericConverter {

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

        private static Enum<? extends DictionaryEnum> parseStr2Enum(String sourceString, Class<?> targetEnumClass) {
            // String -> Enum
            Class<?> identifyType = getEnumItemIdClass(targetEnumClass);
            Class<? extends Enum<? extends DictionaryEnum<?, String>>> enumClass = (Class<? extends Enum<? extends DictionaryEnum<?, String>>>) targetEnumClass;
            if (identifyType == String.class) {

                // 1. fromId
                return DictionaryEnum.decideActualEnum(enumClass.getEnumConstants(), sourceString, DictionaryEnum.compareWithId(),
                        // 2. from name with
                        (enumCls, sourceStr) -> parseStrToIntEnum(sourceString, targetEnumClass));
            }
            return parseStrToIntEnum(sourceString, targetEnumClass);
        }

        private static Enum<? extends DictionaryEnum<?, String>> parseStrToIntEnum(String sourceString, Class<?> targetEnumClass) {
            Class<? extends Enum<? extends DictionaryEnum<?, String>>> enumClass = (Class<? extends Enum<? extends DictionaryEnum<?, String>>>) targetEnumClass;
            return DictionaryEnum.decideActualEnum((enumClass).getEnumConstants(), sourceString, DictionaryEnum.compareWithEnumCodingName(),
                    (enumCls2, sourceStr2) -> {
                        // 3. 兜底判断是否为数字，尝试用数字转换
                        if (StringUtils.isNumeric((String) sourceStr2)) {
                            int intVal = Integer.parseInt((String) sourceStr2);
                            return (Enum<? extends DictionaryEnum<?, String>>) parseInt2Enum(intVal, targetEnumClass);
                        }
                        // 找不到，肯定输入和当前代码版本不一致且这种使用方式无法兼容，报错
                        return DictionaryEnum.onMissMatch(enumCls2, sourceStr2);
                    });
        }

        public static Object parseInt2Enum(Integer sourceInteger, Class<?> targetEnumClass) {
            // int -> Enum
            Class<?> identifyType = getEnumItemIdClass(targetEnumClass);
            if (identifyType == Integer.class) {
                // 1. fromId
                return DictionaryEnum.fromId((Class<? extends Enum<? extends DictionaryEnum<?, Integer>>>) targetEnumClass, sourceInteger);
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


    /**
     * Core.Dictionary -> String
     */
    public static class DictionaryItemToStrGenericConverter implements ConditionalGenericConverter {

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

    /**
     * Enum -> Int/String
     */
    public static class DictionaryEnumSerialGenericConverter implements ConditionalGenericConverter {
        public static final DictionaryEnumSerialGenericConverter INSTANCE = new DictionaryEnumSerialGenericConverter();

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Set.of(new ConvertiblePair(DictionaryItem.class, String.class),
                    new ConvertiblePair(DictionaryItem.class, Integer.class));
        }

        @Override
        public boolean matches(TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
            return sourceType.getType().isEnum();
        }

        @Override
        public Object convert(Object source, @NonNull TypeDescriptor sourceType, @NonNull TypeDescriptor targetType) {
            if (source == null) {
                return null;
            }
            Class<?> sourceEnumClass = sourceType.getType();
            Class<?> targetIntStringClass = targetType.getType();
            Class<?> identifyType = getEnumItemIdClass(sourceEnumClass);
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

    private static Class<?> getEnumItemIdClass(Class<?> enumClass) {
        // 第二个泛型是 itemId 类型
        return Optional.ofNullable(GenericTypeResolver.resolveTypeArguments(enumClass, DictionaryEnum.class)).orElseThrow()[1];
    }
}
