package org.shoulder.core.log.logback.pattern;

import org.apache.commons.lang3.time.FastDateFormat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 带缓存的时间格式化器
 *
 * @author lym
 */
public class CachingFastDateFormatter {

    private final FastDateFormat dateFormat;

    private ZoneId zoneId = TimeZone.getDefault().toZoneId();

    private long lastTimestamp = -1;

    private String cachedStr = null;

    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public CachingFastDateFormatter(String pattern) {
        dateFormat = FastDateFormat.getInstance(pattern);
    }

    public final String format(long now) {
        readWriteLock.readLock().lock();
        long lastTimestamp = this.lastTimestamp;
        String cachedStr = this.cachedStr;
        readWriteLock.readLock().unlock();
        if (lastTimestamp == now) {
            return cachedStr;
        }
        cachedStr = dateFormat.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(now), zoneId));
        readWriteLock.writeLock().lock();
        this.lastTimestamp = now;
        this.cachedStr = cachedStr;
        readWriteLock.writeLock().unlock();
        return cachedStr;
    }

    public void setTimeZone(TimeZone tz) {
        zoneId = tz.toZoneId();
    }

}
