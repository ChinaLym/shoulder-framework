package org.shoulder.core.cache;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

/**
 * spring cache 装饰器
 *
 * @author lym
 */
public class CacheDecorate implements Cache {

    private final org.springframework.cache.Cache cache;

    public CacheDecorate(org.springframework.cache.Cache cache) {
        this.cache = cache;
    }

    @Nonnull
    @Override
    public String getName() {
        return cache.getName();
    }

    @Nonnull
    @Override
    public Object getNativeCache() {
        return cache.getNativeCache();
    }

    @Override
    public ValueWrapper get(@Nonnull Object key) {
        return cache.get(key);
    }

    @Override
    public <T> T get(@Nonnull Object key, Class<T> type) {
        return cache.get(key, type);
    }

    @Override
    public <T> T get(@Nonnull Object key, @Nonnull Callable<T> valueLoader) {
        return cache.get(key, valueLoader);
    }

    @Override
    public void put(@Nonnull Object key, Object value) {
        cache.put(key, value);
    }

    @Override
    public void evict(@Nonnull Object key) {
        cache.evict(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
