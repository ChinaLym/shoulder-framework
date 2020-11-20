package org.shoulder.core.converter;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Controller 方法 String 类型入参自动转为日期类型
 *
 * @author lym
 */
public class LocalDateConverter extends BaseLocalDateTimeConverter<LocalDate> {

    @Override
    protected Map<String, String> initTimeParserMap() {
        Map<String, String> formatMap = new LinkedHashMap<>(2);
        formatMap.put("yyyy-MM-dd", "^\\d{4}-\\d{1,2}-\\d{1,2}$");
        formatMap.put("yyyy/MM/dd", "^\\d{4}/\\d{1,2}/\\d{1,2}$");
        return formatMap;
    }

    @Override
    protected String toStandFormat(@Nonnull String sourceDateString) {
        return super.toStandYearMonthDay(sourceDateString);
    }

    @Override
    protected BiFunction<String, DateTimeFormatter, LocalDate> parseFunction() {
        return LocalDate::parse;
    }

}

