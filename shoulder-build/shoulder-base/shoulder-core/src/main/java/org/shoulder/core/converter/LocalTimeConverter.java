package org.shoulder.core.converter;

import jakarta.annotation.Nonnull;
import org.springframework.core.convert.converter.Converter;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller 方法 String 类型入参自动转为日期类型
 *
 * @author lym
 * @deprecated 1.0 在 Spring 新版本中已经内置支持了
 */
public class LocalTimeConverter extends BaseDateConverter<LocalTime> implements Converter<String, LocalTime> {

    public static final LocalTimeConverter INSTANCE = new LocalTimeConverter();

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    protected Map<String, String> initTimeParserMap() {
        Map<String, String> formatMap = new LinkedHashMap<>(3);
        formatMap.put("HH:mm:ss", "^\\d{1,2}:\\d{2}:\\d{2}$");
        formatMap.put("HH:mm:ss.SSS", "^\\d{1,2}:\\d{2}:\\d{2}.\\d{1,3}$");
        return formatMap;
    }

    @Override
    protected LocalTime fromInstant(Instant instant) {
        return LocalTime.ofInstant(instant, ZoneId.systemDefault());
    }

    @Override
    protected LocalTime parseDateOrTime(@Nonnull String sourceDateString, String dateTimeTemplate) {
        return LocalTime.parse(sourceDateString, DateTimeFormatter.ofPattern(dateTimeTemplate));
    }

}
