package org.shoulder.core.log.logback.pattern;

import org.apache.commons.lang3.time.FastDateFormat;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 带缓存的时间格式化器，减小锁的粒度；使用CachingFastDateFormatter；LocalDateTime.ofInstant 替代 new Date
 *
 * @author lym
 */
public class CachingFastDateFormatter {

    private final FastDateFormat dateFormat;

    private volatile long lastTimestamp = -1;

    private volatile String cachedStr = null;

    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    // todo 缓存最近 32ms 的时间模板，以满足大多 GC 时间
    private volatile TimeFormatCache cache;

    public CachingFastDateFormatter(String pattern) {
        dateFormat = FastDateFormat.getInstance(pattern);
    }

    public final String format(long now) {
        TimeFormatCache lastCache = this.cache;
        if (lastCache.timestamp == now) {
            return lastCache.formatStr;
        }
        cachedStr = dateFormat.format(now);

        // todo cas set cache

        return cachedStr;
    }


    public static class TimeFormatCache {

        final long timestamp;

        final String formatStr;

        public TimeFormatCache(long timestamp, String formatStr) {
            this.timestamp = timestamp;
            this.formatStr = formatStr;
        }
    }

}
