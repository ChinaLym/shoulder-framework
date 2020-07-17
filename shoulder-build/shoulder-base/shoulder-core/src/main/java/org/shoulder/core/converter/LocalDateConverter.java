package org.shoulder.core.converter;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 解决入参为 Date类型
 *
 * @author lym
 */
public class LocalDateConverter extends BaseDateConverter<LocalDate>{

    @Override
    protected Map<String, String> initTimeParserMap() {
        Map<String, String> formatMap = new LinkedHashMap<>(2);
        formatMap.put("yyyy-MM-dd", "^\\d{4}-\\d{1,2}-\\d{1,2}$");
        formatMap.put("yyyy/MM/dd", "^\\d{4}/\\d{1,2}/\\d{1,2}$");
        return formatMap;
    }

    @Override
    protected LocalDate parseDateOrTime(@NotEmpty String sourceDataString, String dateTimeTemplate){
        return LocalDate.parse(sourceDataString, DateTimeFormatter.ofPattern(dateTimeTemplate));
    }

}

