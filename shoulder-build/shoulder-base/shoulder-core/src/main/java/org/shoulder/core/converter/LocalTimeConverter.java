package org.shoulder.core.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 解决入参为 Date类型
 *
 * @author lym
 */
public class LocalTimeConverter extends BaseDateConverter<LocalTime> implements Converter<String, LocalTime> {


    @Override
    protected Map<String, String> initTimeParserMap() {
        return Collections.singletonMap("yyyy-MM-dd HH:mm:ss", "^\\d{1,2}:\\d{1,2}:\\d{1,2}$");
    }

    @Override
    protected LocalTime parseDateOrTime(@NotEmpty String sourceDataString, String dateTimeTemplate){
        return LocalTime.parse(sourceDataString, DateTimeFormatter.ofPattern(dateTimeTemplate));
    }

}
