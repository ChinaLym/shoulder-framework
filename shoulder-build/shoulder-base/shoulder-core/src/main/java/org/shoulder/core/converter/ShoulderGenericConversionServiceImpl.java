package org.shoulder.core.converter;

import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.boot.autoconfigure.web.format.WebConversionService;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * shoulder 通用转换器 spring 的增强：
 *  1. 增加 conversionServiceList 便于扩展特定通用场景转换（复杂范型场景）
 *  2. 支持 集合批量转换
 *
 * @author lym
 */
public class ShoulderGenericConversionServiceImpl extends WebConversionService implements ShoulderConversionService {

    /**
     * 优先级更高的
     */
    private final List<ConversionService> conversionServiceList = new ArrayList<>(3);

    public ShoulderGenericConversionServiceImpl(DateTimeFormatters dateTimeFormatters) {
        super(dateTimeFormatters);
        registerDictionaryEnumConverters();
        registerJdk8DateConverters();
    }

    private void registerDictionaryEnumConverters() {
        // enum to str
//        addConverter(new DictionaryItem2StringConverter());

//        Collection<Class<? extends Enum<? extends DictionaryEnum>>> allDictionaryEnums =
//                dictionaryEnumStore.listAllTypes();
//        for (Class<? extends Enum<? extends DictionaryEnum>> dictionaryEnumClass : allDictionaryEnums) {
//            Class<?> itemIdClassType = ((DictionaryEnum) dictionaryEnumClass.getEnumConstants()[0]).getItemId().getClass();
//            if (itemIdClassType == Integer.class) {
//                Converter<Integer, ? extends Enum<? extends DictionaryEnum<?, Integer>>> converter = new Integer2DictionaryEnumConverter<>((Class<? extends Enum<? extends DictionaryEnum<?, Integer>>>) dictionaryEnumClass);
//                addConverter((Integer.class)itemIdClassType, (Class<? extends Enum<? extends DictionaryEnum<?, Integer>>>)dictionaryEnumClass, converter);
//            } else if (itemIdClassType == String.class) {
//                addConverter((String.class)itemIdClassType, dictionaryEnumClass,
//                        new String2DictionaryEnumConverter<>((Class<? extends Enum<? extends DictionaryEnum<?, String>>>) dictionaryEnumClass)
//                );
//
//            }
//        }

        // str to enum
//        List<? extends Class<? extends Enum<? extends DictionaryEnum<?, ?>>>> enumClassList =
//                Arrays.stream(DictionaryTypeEnum.values())
//                        .map(DictionaryTypeEnum::getEnumClass)
//                        .collect(Collectors.toList());
//        for (Class<? extends Enum<? extends DictionaryEnum<?, ?>>> enumClass : enumClassList) {
//            String2DictionaryEnumConverter converter = new String2DictionaryEnumConverter<>(enumClass);
//            addConverter(String.class, enumClass, converter);
//        }
        // str 无法到 dynamicDictionary，必须手动转换的暂时没有（）

    }

    private void registerJdk8DateConverters() {
        // convert to each
        //addConverter(Date.class, Instant.class, Date::toInstant);
        addConverter(Date.class, LocalDateTime.class, d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        addConverter(Date.class, LocalDate.class, d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

        //addConverter(Instant.class, Date.class, Date::from);
        addConverter(Instant.class, LocalDateTime.class, i -> LocalDateTime.ofInstant(i, ZoneId.systemDefault()));
        addConverter(Instant.class, LocalDate.class,
                in -> LocalDateTime.ofInstant(in, ZoneId.systemDefault()).toLocalDate());

        addConverter(LocalDateTime.class, Date.class, ldt -> convert(convert(ldt, Instant.class), Date.class));
        addConverter(LocalDateTime.class, Instant.class, t -> t.toInstant(ZoneOffset.UTC));
        addConverter(LocalDateTime.class, LocalDate.class, LocalDateTime::toLocalDate);

        addConverter(LocalDate.class, Date.class, d -> convert(convert(d, LocalDateTime.class), Date.class));
        addConverter(LocalDate.class, LocalDateTime.class, LocalDate::atStartOfDay);
        addConverter(LocalDate.class, Instant.class, d -> convert(convert(d, LocalDateTime.class), Instant.class));

        // parse 时间戳
        //addConverter(Long.class, Instant.class, instant -> instant <= 9999999999L ? Instant.ofEpochSecond(instant) : Instant.ofEpochMilli(instant));
        //addConverter(Long.class, Date.class, d -> convert(convert(d, Instant.class), Date.class));

        // parse Str
        addConverter(String.class, Date.class, new DateConverter());
        addConverter(String.class, Instant.class, s -> {
            Date d = convert(s, Date.class);
            return d == null ? null : d.toInstant();
        });
        addConverter(String.class, LocalTime.class, new LocalTimeConverter());
        addConverter(String.class, LocalDateTime.class, new LocalDateTimeConverter());
        addConverter(String.class, LocalDate.class, new LocalDateConverter());

        // formatter
        //addConverter(Date.class, String.class, DateConverter.FORMATTER::format);
        //addConverter(LocalTime.class, String.class, LocalTimeConverter.FORMATTER::format);
        //addConverter(LocalDateTime.class, String.class, LocalDateTimeConverter.FORMATTER::format);
        //addConverter(Instant.class, String.class, source -> LocalDateTimeConverter.FORMATTER.format(convert(source, LocalDateTime.class)));
        //addConverter(LocalDate.class, String.class, LocalDateConverter.FORMATTER::format);
    }

    @Override
    public Object convert(@Nullable Object source, @Nullable TypeDescriptor sourceType, TypeDescriptor targetType) {
        // 特殊支持从 int / string 转换为 DictionaryEnum
        Assert.notNull(targetType, "Target type to convert to cannot be null");
        for (ConversionService conversionService : conversionServiceList) {
            if(conversionService.canConvert(sourceType, targetType)) {
                return conversionService.convert(source, sourceType, targetType);
            }
        }
        return super.convert(source, sourceType, targetType);
    }

    @Override
    public <S, T> List<T> convert(Collection<? extends S> sourceCollection, Class<T> targetType) {
        if (sourceCollection == null) {
            return null;
        }
        List<T> listResult = new ArrayList<>(sourceCollection.size());

        // collection 的元素为 NULL
        TypeDescriptor sourceTypeDescriptor = null;
        TypeDescriptor targetTypeDescriptor = TypeDescriptor.valueOf(targetType);
        for (S source : sourceCollection) {
            if (source == null) {
                listResult.add(null);
            } else {
                if (sourceTypeDescriptor == null) {
                    sourceTypeDescriptor = TypeDescriptor.valueOf(source.getClass());
                }
                listResult.add((T) convert(source, sourceTypeDescriptor, targetTypeDescriptor));

            }
        }
        return listResult;
    }

    @SuppressWarnings("rawtypes")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshedEvent(ContextRefreshedEvent event) {
        //Collection<BaseDataConverter> dataConverters = event.getApplicationContext().getBeansOfType(BaseDataConverter.class)
        //        .values();
        //dataConverters.forEach(this::addConverter);
    }

    @Override
    public void addConverter(Converter<?, ?> converter) {
        //if (converter instanceof BaseDataConverter) {
        //    ((BaseDataConverter) converter).setConversionService(this);
        //}
        super.addConverter(converter);
    }

    public void addConversionService(ConversionService conversionService) {
        conversionServiceList.add(conversionService);
    }

    public List<ConversionService> getConversionServiceList() {
        return conversionServiceList;
    }
}
