package org.shoulder.data.dal.sequence.dao;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.shoulder.data.dal.sequence.model.DoubleSequenceRange;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 序列-内存缓存
 * 内置双 buffer
 *
 * @author lym
 */
public class SequenceRangeCache {

    /**
     * 缓存数
     */
    public static final long DEFAULT_CACHE_SIZE = 20000;

    /**
     * 默认缓存过期时间-每次启动/每个进程过期时间随机
     */
    public static final long DEFAULT_CACHE_EXPIRE_SECONDS = 24 * 60 * 60 + ThreadLocalRandom.current().nextInt(3600);

    /**
     * 缓存
     * key：sequenceName-dbSharding-tableSharding
     * value:
     */
    private final Cache<String, DoubleSequenceRange> cache;

    public SequenceRangeCache() {
        this(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_EXPIRE_SECONDS);
    }

    public SequenceRangeCache(long cacheSize, long cacheExpireSeconds) {
        this.cache = CacheBuilder.newBuilder()
            .maximumSize(cacheSize)
            .expireAfterWrite(cacheExpireSeconds, TimeUnit.SECONDS)
            .build();
    }

    public DoubleSequenceRange get(String key) {
        return cache.getIfPresent(key);
    }

    public void put(String key, DoubleSequenceRange sequenceRange) {
        cache.put(key, sequenceRange);
    }

    public void invalidate(String key) {
        cache.invalidate(key);
    }

    public void invalidateAll(Iterable<?> keys) {
        cache.invalidateAll(keys);
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }

    public boolean isEmpty() {
        return cache.size() == 0;
    }

    public long size() {
        return cache.size();
    }

    public Map<String, DoubleSequenceRange> asMap() {
        return cache.asMap();
    }
}
