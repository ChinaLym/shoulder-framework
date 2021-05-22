package org.shoulder.core.converter;

import org.apache.commons.lang3.time.FastDateFormat;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;

import javax.annotation.Nonnull;
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
public class DateConverter extends BaseDateConverter<Date> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final DateConverter INSTANCE = new DateConverter();

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
        // UTC: yyyy-MM-dd'T'HH:mm:ss.SSSXXX  yyyy-MM-dd'T'HH:mm:ss.SSS Z
        formatMap.put(AppInfo.UTC_DATE_TIME_FORMAT, "^\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}");
        formatMap.put("yyyy/MM", "^\\d{4}/\\d{1,2}$");
        formatMap.put("yyyy/MM/dd", "^\\d{4}/\\d{1,2}/\\d{1,2}$");
        formatMap.put("yyyy/MM/dd HH", "^\\d{4}/\\d{1,2}/\\d{1,2} {1}\\d{1,2}");
        formatMap.put("yyyy/MM/dd HH:mm", "^\\d{4}/\\d{1,2}/\\d{1,2} {1}\\d{1,2}:\\d{1,2}$");
        formatMap.put("yyyy/MM/dd HH:mm:ss", "^\\d{4}/\\d{1,2}/\\d{1,2} {1}\\d{1,2}:\\d{1,2}:\\d{1,2}$");
        return formatMap;
    }


    @Override
    protected Date parseDateOrTime(@Nonnull String sourceDateString, String dateTimeTemplate) {
        try {
            return FastDateFormat.getInstance(dateTimeTemplate).parse(sourceDateString);
        } catch (ParseException e) {
            log.info("dateFormatError, date={}, format={}", sourceDateString, dateTimeTemplate, e);
            throw new DateTimeParseException("Text '" + sourceDateString + "' could not be parsed: " + e.getMessage(), sourceDateString, 0, e);
        }
    }

}
