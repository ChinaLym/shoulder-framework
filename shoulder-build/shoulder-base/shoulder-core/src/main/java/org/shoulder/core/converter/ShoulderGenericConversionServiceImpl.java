package org.shoulder.core.converter;

import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.boot.autoconfigure.web.format.WebConversionService;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.Nullable;

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
        registerJdk8DateConverters();
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
        //Assert.notNull(targetType, "Target type to convert to cannot be null");
        //for (ConversionService conversionService : conversionServiceList) {
        //    if(conversionService.canConvert(sourceType, targetType)) {
        //        return conversionService.convert(source, sourceType, targetType);
        //    }
        //}
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

    public void addConversionService(ConversionService conversionService) {
        conversionServiceList.add(conversionService);
    }

    public List<ConversionService> getConversionServiceList() {
        return conversionServiceList;
    }
}
