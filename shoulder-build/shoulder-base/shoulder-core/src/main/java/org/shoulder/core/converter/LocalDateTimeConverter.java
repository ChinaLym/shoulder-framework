package org.shoulder.core.converter;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
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

    @Override
    protected Map<String, String> initTimeParserMap() {
        Map<String, String> formatMap = new LinkedHashMap<>(2);
        formatMap.put("yyyy-MM-dd HH:mm:ss", "^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{2}:\\d{2}:\\d{2}$");
        formatMap.put("yyyy/MM/dd HH:mm:ss", "^\\d{4}/\\d{1,2}/\\d{1,2} {1}\\d{2}:\\d{2}:\\d{2}$");
        return formatMap;
    }

    @Override
    protected String toStandFormat(@Nonnull String sourceDateString) {
        if (sourceDateString.length() == 19) {
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
