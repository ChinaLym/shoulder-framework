package org.shoulder.core.log.logback.pattern;

import ch.qos.logback.core.util.CachingDateFormatter;
import org.apache.commons.lang3.time.FastDateFormat;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * 带缓存的时间格式化器，去掉了锁。LocalDateTime.ofInstant 替代 new Date
 *
 * @see CachingDateFormatter 可以对比 logback 的实现，logback中使用了 synchronized 代码块，而日志打印必定会有多个线程竞争，导致阻塞，Shoulder中去掉了锁
 * @author lym
 */
public class CachingFastDateFormatter {

    private final FastDateFormat dateFormat;

    /**
     * 缓存最近的时间戳模板，使用数组以避免YGC带来的缓存雪崩
     */
    private final TimeFormatCache[] cache;

    private final VarHandle cacheHandle = MethodHandles.arrayElementVarHandle(CachingFastDateFormatter.TimeFormatCache[].class);

    public CachingFastDateFormatter(String pattern) {
        // 默认缓存 32ms 以抵消大多数应用的大多数 YGC 耗时带来的缓存雪崩效应
        this(pattern, 32);
    }

    /**
     * 构造
     *
     * @param pattern   时间格式
     * @param cacheSize 缓存时间，通常为绝大多数 ygc 时间，2次幂
     */
    public CachingFastDateFormatter(String pattern, int cacheSize) {
        dateFormat = FastDateFormat.getInstance(pattern);
        cache = new TimeFormatCache[cacheSizeFor(cacheSize)];
    }

    /**
     * Returns a power of two table size for the given desired capacity.
     */
    private static int cacheSizeFor(int s) {
        int maximumCapacity = 1 << 30;
        int n = -1 >>> Integer.numberOfLeadingZeros(s - 1);
        return (n < 0) ? 1 : (n >= maximumCapacity) ? maximumCapacity : n + 1;
    }


    public final String format(long now) {
        int index = ((int) (now)) & (cache.length - 1);
        TimeFormatCache lastCache = getAt(index);

        if (lastCache.timestamp == now) {
            return lastCache.formatStr;
        } else {
            String dataFormat = dateFormat.format(now);
            // 上下文中无其他可共享变量，无需强制可见性；这里无需一定成功，仅尝试一次
            weakCasAt(index, lastCache, new TimeFormatCache(now, dataFormat));
            return dataFormat;
        }

    }

    private TimeFormatCache getAt(int index) {
        return (TimeFormatCache) cacheHandle.get(cache, index);
    }

    /**
     * 弱 CAS，没有强制 happens before前后变量的可变性
     */
    private boolean weakCasAt(int index, TimeFormatCache old, TimeFormatCache newValue) {
        return cacheHandle.weakCompareAndSet(cache, index, old, newValue);
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
