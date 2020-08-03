package org.shoulder.core.log.logback.pattern;

import org.apache.commons.lang3.time.FastDateFormat;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

/**
 * 带缓存的时间格式化器，减小锁的粒度；使用CachingFastDateFormatter；LocalDateTime.ofInstant 替代 new Date
 * fixme {@link StampedLock}
 * @author lym
 */
public class CachingFastDateFormatter {

    private final FastDateFormat dateFormat;

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
        cachedStr = dateFormat.format(now);
        readWriteLock.writeLock().lock();
        this.lastTimestamp = now;
        this.cachedStr = cachedStr;
        readWriteLock.writeLock().unlock();
        return cachedStr;
    }

}
