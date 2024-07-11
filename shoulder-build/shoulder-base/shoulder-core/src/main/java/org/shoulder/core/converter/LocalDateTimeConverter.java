package org.shoulder.core.converter;

import jakarta.annotation.Nonnull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;


/**
 * String -> Date，且支持如 2020-6-6 这类非标准日期
 *
 * @author lym
 */
public class LocalDateTimeConverter extends BaseLocalDateTimeConverter<LocalDateTime> {

    public static final LocalDateTimeConverter INSTANCE = new LocalDateTimeConverter();

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected Map<String, String> initTimeParserMap() {
        Map<String, String> formatMap = new LinkedHashMap<>(10);

        formatMap.put("yyyy-MM-dd HH:mm:ss.SSS", "^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{2}:\\d{2}:\\d{2}.\\d{1,3}$");
        formatMap.put("yyyy-MM-dd HH:mm:ss", "^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{2}:\\d{2}:\\d{2}$");
        formatMap.put("yyyy/MM/dd HH:mm:ss.SSS", "^\\d{4}/\\d{1,2}/\\d{1,2} {1}\\d{2}:\\d{2}:\\d{2}.\\d{1,3}$");
        formatMap.put("yyyy/MM/dd HH:mm:ss", "^\\d{4}/\\d{1,2}/\\d{1,2} {1}\\d{2}:\\d{2}:\\d{2}$");

        return formatMap;
    }

    @Override
    protected LocalDateTime fromInstant(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    @Nonnull
    @Override
    protected String toStandFormat(@Nonnull String sourceDateString) {
        return toStandDateFormat(sourceDateString, true);
    }

    @Override
    protected BiFunction<String, DateTimeFormatter, LocalDateTime> parseFunction() {
        return LocalDateTime::parse;
    }

}
