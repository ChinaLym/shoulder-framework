package org.shoulder.core.converter;

import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.time.FastDateFormat;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.ShoulderLoggers;

import java.text.ParseException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * String -> Date，且支持如 2020-6-6 这类非标准日期
 *
 * @author lym
 */
public class DateConverter extends BaseDateConverter<Date> {

    private final Logger log = ShoulderLoggers.SHOULDER_DEFAULT;

    public static final DateConverter INSTANCE = new DateConverter();

    public static FastDateFormat FORMATTER = FastDateFormat.getInstance(AppInfo.dateTimeFormat());

    /**
     * 严格模式，默认关
     */
    protected boolean lenientMode = false;

    @Override
    protected Map<String, String> initTimeParserMap() {
        Map<String, String> formatMap = new LinkedHashMap<>(16);
        formatMap.put("yyyy", "^\\d{4}");
        formatMap.put("yyyy-MM", "^\\d{4}-\\d{1,2}$");
        formatMap.put("yyyy-MM-dd", "^\\d{4}-\\d{1,2}-\\d{1,2}$");
        formatMap.put("yyyy-MM-dd HH", "^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{1,2}");
        formatMap.put("yyyy-MM-dd HH:mm", "^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{1,2}:\\d{1,2}$");
        formatMap.put("yyyy-MM-dd HH:mm:ss", "^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{1,2}:\\d{1,2}:\\d{1,2}$");
        formatMap.put("yyyy-MM-dd HH:mm:ss.SSS", "^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{1,3}$");
        // UTC: yyyy-MM-dd'T'HH:mm:ss.SSSXXX  yyyy-MM-dd'T'HH:mm:ss.SSS Z
        formatMap.put("yyyy/MM", "^\\d{4}/\\d{1,2}$");
        formatMap.put("yyyy/MM/dd", "^\\d{4}/\\d{1,2}/\\d{1,2}$");
        formatMap.put("yyyy/MM/dd HH", "^\\d{4}/\\d{1,2}/\\d{1,2} {1}\\d{1,2}");
        formatMap.put("yyyy/MM/dd HH:mm", "^\\d{4}/\\d{1,2}/\\d{1,2} {1}\\d{1,2}:\\d{1,2}$");
        formatMap.put("yyyy/MM/dd HH:mm:ss", "^\\d{4}/\\d{1,2}/\\d{1,2} {1}\\d{1,2}:\\d{1,2}:\\d{1,2}$");
        formatMap.put("yyyy/MM/dd HH:mm:ss.SSS", "^\\d{4}/\\d{1,2}/\\d{1,2} {1}\\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{1,3}$");
        return formatMap;
    }

    @Override
    protected Date fromInstant(Instant instant) {
        return Date.from(instant);
    }


    @Nonnull
    @Override
    protected String toStandFormat(@Nonnull String sourceDateString) {
        return toStandDateFormat(sourceDateString, false);
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
