package org.shoulder.core.converter;

import jakarta.annotation.Nonnull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * String -> LocalDate，且支持如 2020-6-6 这类非标准日期
 *
 * @author lym
 */
public class LocalDateConverter extends BaseLocalDateTimeConverter<LocalDate> {

    public static final LocalDateConverter INSTANCE = new LocalDateConverter();

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    protected Map<String, String> initTimeParserMap() {
        Map<String, String> formatMap = new LinkedHashMap<>(2);
        formatMap.put("yyyy-MM-dd", "^\\d{4}-\\d{1,2}-\\d{1,2}$");
        formatMap.put("yyyy/MM/dd", "^\\d{4}/\\d{1,2}/\\d{1,2}$");
        return formatMap;
    }

    @Override
    protected LocalDate fromInstant(Instant instant) {
        return LocalDate.ofInstant(instant, ZoneId.systemDefault());
    }

    @Nonnull
    @Override
    protected String toStandFormat(@Nonnull String sourceDateString) {
        return toStandYearMonthDay(sourceDateString);
    }

    @Override
    protected BiFunction<String, DateTimeFormatter, LocalDate> parseFunction() {
        return LocalDate::parse;
    }

}

