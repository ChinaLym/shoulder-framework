package org.shoulder.log.operation.logger.logback.pattern;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.pattern.DateConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;

import java.lang.reflect.Method;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 用 {@link CachingFastDateFormatter} 替代默认实现的时间格式化; logback默认实现时区启动后将不再改变，该类会每60s刷新一次
 *
 * @author lym
 * @see DateConverter
 */
public class TimeZoneLazyAwareDateConverter extends ClassicConverter {

    private CachingFastDateFormatter dateFormatter = null;

    /**
     * 刷新时区的周期，单位秒
     */
    private static final int UPDATE_TIMEZONE_PERIOD = 60;

    private final Method GET_SYSTEM_GMT_OFF_SET_ID_METHOD;

    public TimeZoneLazyAwareDateConverter() {
        Method gtsyszMethod = null;
        try {
            // native invoke
            gtsyszMethod = TimeZone.class.getDeclaredMethod("getSystemGMTOffsetID");
            gtsyszMethod.setAccessible(true);
        } catch (Exception e) {
            addWarn("get native method fail", e);
        }
        GET_SYSTEM_GMT_OFF_SET_ID_METHOD = gtsyszMethod;
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

        //use default
        dateFormatter.setTimeZone(TimeZone.getDefault());

        if (GET_SYSTEM_GMT_OFF_SET_ID_METHOD != null) {
            //start a schedule for update timezone
            ScheduledExecutorService timezoneTaskExecutor =
                Executors.newSingleThreadScheduledExecutor((r) -> {
                    Thread thread = Executors.defaultThreadFactory().newThread(r);
                    thread.setDaemon(true);
                    thread.setName("timezoneRefreshTimer");
                    return thread;
                });
            timezoneTaskExecutor.scheduleAtFixedRate(new TimezoneRefreshTask(),
                TimeZoneLazyAwareDateConverter.UPDATE_TIMEZONE_PERIOD,
                TimeZoneLazyAwareDateConverter.UPDATE_TIMEZONE_PERIOD, TimeUnit.SECONDS);
        }

    }

    @Override
    public String convert(ILoggingEvent le) {
        long timestamp = le.getTimeStamp();
        return dateFormatter.format(timestamp);
    }

    class TimezoneRefreshTask implements Runnable {
        @Override
        public void run() {
            synchronized (TimeZoneLazyAwareDateConverter.class) {
                TimeZone defaultTimeZone = refreshAndGetCurrentTimeZone();
                //if zone is null, do next time
                if (defaultTimeZone != null) {
                    dateFormatter.setTimeZone(defaultTimeZone);
                }
            }
        }

        private TimeZone refreshAndGetCurrentTimeZone() {
            TimeZone timeZoneRef = refreshAndGetCurrentTimeZoneRef();
            if (timeZoneRef == null) {
                return TimeZone.getDefault();
            }
            return (TimeZone) timeZoneRef.clone();
        }

        private TimeZone refreshAndGetCurrentTimeZoneRef() {
            if (GET_SYSTEM_GMT_OFF_SET_ID_METHOD != null) {
                //use TimeZone.getSystemGMTOffsetID get system current timeZone
                try {
                    Object result = GET_SYSTEM_GMT_OFF_SET_ID_METHOD.invoke(this);
                    if (result instanceof String) {
                        return TimeZone.getTimeZone((String) result);
                    }
                } catch (Exception e) {
                    // If this were to fail, get default.
                }
            }
            return TimeZone.getDefault();
        }
    }

}

