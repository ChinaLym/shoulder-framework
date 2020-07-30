package org.shoulder.core.log.logback.pattern;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.pattern.DateConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;

/**
 * 用 {@link CachingFastDateFormatter} 替代默认实现的时间格式化; 减小锁的粒度
 *
 * @author lym
 * @see DateConverter
 */
public class ShoulderDateConverter extends ClassicConverter {

    private CachingFastDateFormatter dateFormatter = null;

    @Override
    public String convert(ILoggingEvent le) {
        long timestamp = le.getTimeStamp();
        return dateFormatter.format(timestamp);
    }

    @Override
    public void start() {

        String datePattern = getFirstOption();
        if (datePattern == null) {
            datePattern = CoreConstants.ISO8601_PATTERN;
        }

        if (datePattern.equals(CoreConstants.ISO8601_STR)) {
            datePattern = CoreConstants.ISO8601_PATTERN;
        }

        try {
            dateFormatter = new CachingFastDateFormatter(datePattern);
        } catch (IllegalArgumentException e) {
            addWarn("Fallback to ISO8601 for could not instantiate CachingFastDateFormatter with pattern " + datePattern, e);
            // default to the ISO8601 format
            dateFormatter = new CachingFastDateFormatter(CoreConstants.ISO8601_PATTERN);
        }

    }

}

