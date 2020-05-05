package org.shoulder.core.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 解决入参为 Date类型
 *
 * @author lym
 */
public class String2LocalDateConverter extends BaseDateConverter<LocalDate> implements Converter<String, LocalDate> {

    protected static final Map<String, String> FORMAT = new LinkedHashMap<>(2);

    static {
        FORMAT.put("yyyy-MM-dd", "^\\d{4}-\\d{1,2}-\\d{1,2}$");
        FORMAT.put("yyyy/MM/dd", "^\\d{4}/\\d{1,2}/\\d{1,2}$");
    }

    @Override
    protected Map<String, String> getSupportPatterns() {
        return FORMAT;
    }

    @Override
    public LocalDate convert(@NonNull String source) {
        return super.convert(source, (key) -> LocalDate.parse(source, DateTimeFormatter.ofPattern(key)));
    }

}

