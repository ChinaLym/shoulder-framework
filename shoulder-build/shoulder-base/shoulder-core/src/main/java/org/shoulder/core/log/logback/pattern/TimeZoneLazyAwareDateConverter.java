package org.shoulder.core.log.logback.pattern;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.pattern.DateConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import org.springframework.lang.NonNull;

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

    private final Method GET_SYSTEM_GMT_OFF_SET_ID_METHOD = null;

    /*public TimeZoneLazyAwareDateConverter() {
        Method gtsyszMethod = null;
        try {
            // native invoke
            gtsyszMethod = TimeZone.class.getDeclaredMethod("getSystemGMTOffsetID");
            gtsyszMethod.setAccessible(true);
        } catch (Exception e) {
            addWarn("get native method fail", e);
        }
        GET_SYSTEM_GMT_OFF_SET_ID_METHOD = gtsyszMethod;
    }*/

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
            timezoneTaskExecutor.scheduleAtFixedRate(() -> {
                    synchronized (TimeZoneLazyAwareDateConverter.class) {
                        dateFormatter.setTimeZone(currentTimeZone());
                    }
                },
                TimeZoneLazyAwareDateConverter.UPDATE_TIMEZONE_PERIOD,
                TimeZoneLazyAwareDateConverter.UPDATE_TIMEZONE_PERIOD, TimeUnit.SECONDS);
        }

    }

    /**
     * 刷新时区
     * 该方法使用了反射，故需放在该类中才有权限
     */
    @NonNull
    private TimeZone currentTimeZone() {
        if (GET_SYSTEM_GMT_OFF_SET_ID_METHOD != null) {
            try {
                Object result = GET_SYSTEM_GMT_OFF_SET_ID_METHOD.invoke(this);
                if (result instanceof String) {
                    return (TimeZone) TimeZone.getTimeZone((String) result).clone();
                }
            } catch (Exception ignored) {
                // will return default timezone
            }
        }
        return TimeZone.getDefault();

    }

}

