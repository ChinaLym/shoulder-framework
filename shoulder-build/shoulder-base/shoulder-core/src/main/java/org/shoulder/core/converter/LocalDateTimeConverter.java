package org.shoulder.core.converter;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;


/**
 * Controller 方法 String 类型入参自动转为日期类型
 *
 * @author lym
 */
public class LocalDateTimeConverter extends BaseLocalDateTimeConverter<LocalDateTime> {

    public static final LocalDateTimeConverter INSTANCE = new LocalDateTimeConverter();

    @Override
    protected Map<String, String> initTimeParserMap() {
        Map<String, String> formatMap = new LinkedHashMap<>(6);
        // normal
        formatMap.put("yyyy-MM-dd HH:mm:ss", "^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{2}:\\d{2}:\\d{2}$");
        formatMap.put("yyyy/MM/dd HH:mm:ss", "^\\d{4}/\\d{1,2}/\\d{1,2} {1}\\d{2}:\\d{2}:\\d{2}$");
        // ISO
        formatMap.put("yyyy-MM-dd'T'HH:mm:ss", "^\\d{4}-\\d{1,2}-\\d{1,2}T{1}\\d{2}:\\d{2}:\\d{2}$");
        formatMap.put("yyyy/MM/ddTHH:mm:ss", "^\\d{4}/\\d{1,2}/\\d{1,2}T{1}\\d{2}:\\d{2}:\\d{2}$");
        // simple
        formatMap.put("yyyy-MM-dd", "^\\d{4}-\\d{1,2}-\\d{1,2}$");
        formatMap.put("yyyy/MM/dd", "^\\d{4}/\\d{1,2}/\\d{1,2}$");
        return formatMap;
    }

    @Override
    protected LocalDateTime fromInstant(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    @Nonnull
    @Override
    protected String toStandFormat(@Nonnull String sourceDateString) {
        // format 长度
        if (sourceDateString.length() == 19 || sourceDateString.length() == 10) {
            return sourceDateString;
        } else {
            String[] dateTimeParts = sourceDateString.split(" ");
            String date = dateTimeParts[0];
            String time = dateTimeParts[1];
            return super.toStandYearMonthDay(date) + " " + time;
        }
    }

    @Override
    protected BiFunction<String, DateTimeFormatter, LocalDateTime> parseFunction() {
        return LocalDateTime::parse;
    }

}
