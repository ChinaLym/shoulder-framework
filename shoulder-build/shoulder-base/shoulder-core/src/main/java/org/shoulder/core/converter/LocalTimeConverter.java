package org.shoulder.core.converter;

import org.springframework.core.convert.converter.Converter;

import javax.annotation.Nonnull;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

/**
 * Controller 方法 String 类型入参自动转为日期类型
 *
 * @author lym
 */
public class LocalTimeConverter extends BaseDateConverter<LocalTime> implements Converter<String, LocalTime> {


    public static final LocalTimeConverter INSTANCE = new LocalTimeConverter();

    @Override
    protected Map<String, String> initTimeParserMap() {
        return Collections.singletonMap("HH:mm:ss", "^\\d{1,2}:\\d{2}:\\d{2}$");
    }

    @Override
    protected LocalTime parseDateOrTime(@Nonnull String sourceDateString, String dateTimeTemplate) {
        return LocalTime.parse(sourceDateString, DateTimeFormatter.ofPattern(dateTimeTemplate));
    }

}
