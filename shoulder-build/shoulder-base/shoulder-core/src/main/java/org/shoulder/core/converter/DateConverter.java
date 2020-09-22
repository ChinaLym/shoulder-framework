package org.shoulder.core.converter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.FastDateFormat;

import javax.validation.constraints.NotEmpty;
import java.text.ParseException;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller 方法 String 类型入参自动转为日期类型
 *
 * @author lym
 */
@Slf4j
public class DateConverter extends BaseDateConverter<Date> {

    /**
     * 严格模式，默认关
     */
    protected boolean lenientMode = false;

    @Override
    protected Map<String, String> initTimeParserMap() {
        Map<String, String> formatMap = new LinkedHashMap<>(11);
        formatMap.put("yyyy", "^\\d{4}");
        formatMap.put("yyyy-MM", "^\\d{4}-\\d{1,2}$");
        formatMap.put("yyyy-MM-dd", "^\\d{4}-\\d{1,2}-\\d{1,2}$");
        formatMap.put("yyyy-MM-dd HH", "^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{1,2}");
        formatMap.put("yyyy-MM-dd HH:mm", "^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{1,2}:\\d{1,2}$");
        formatMap.put("yyyy-MM-dd HH:mm:ss", "^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{1,2}:\\d{1,2}:\\d{1,2}$");
        formatMap.put("yyyy/MM", "^\\d{4}/\\d{1,2}$");
        formatMap.put("yyyy/MM/dd", "^\\d{4}/\\d{1,2}/\\d{1,2}$");
        formatMap.put("yyyy/MM/dd HH", "^\\d{4}/\\d{1,2}/\\d{1,2} {1}\\d{1,2}");
        formatMap.put("yyyy/MM/dd HH:mm", "^\\d{4}/\\d{1,2}/\\d{1,2} {1}\\d{1,2}:\\d{1,2}$");
        formatMap.put("yyyy/MM/dd HH:mm:ss", "^\\d{4}/\\d{1,2}/\\d{1,2} {1}\\d{1,2}:\\d{1,2}:\\d{1,2}$");
        // "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
        return formatMap;
    }


    @Override
    protected Date parseDateOrTime(@NotEmpty String sourceDateString, String dateTimeTemplate) {
        try {
            return FastDateFormat.getInstance(dateTimeTemplate).parse(sourceDateString);
        } catch (ParseException e) {
            log.info("dateFormatError, date={}, format={}", sourceDateString, dateTimeTemplate, e);
            throw new DateTimeParseException("Text '" + sourceDateString + "' could not be parsed: " + e.getMessage(), sourceDateString, 0, e);
        }
    }

}