package org.shoulder.core.cache;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.support.NullValue;
import org.springframework.lang.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 缓存接口（spring 基础上添加语法糖）
 *
 * @author lym
 */
public interface Cache extends org.springframework.cache.Cache {

    /**
     * 与 {@link org.springframework.cache.Cache#get} 区别：spring 中用 null 表示缓存 miss
     * 用 {@link NullValue#INSTANCE} 表示命中但值为空
     * 但设置值为空，这种用法较难使用，且使用场景较少，每次调用都要拆包，比较麻烦；这里 key 不存在或者 value 为空都返回 null
     *
     * @param key key
     * @return value
     */
    @Nullable
    @SuppressWarnings("unchecked")
    default <T> T getValue(@Nonnull Object key) {
        ValueWrapper valueWrapper = get(key);
        Object result;
        return valueWrapper == null ? null : (result = valueWrapper.get()) == NullValue.INSTANCE ? null : (T) result;
    }

    /**
     * 类似 {@link org.springframework.cache.Cache#get(java.lang.Object, java.util.concurrent.Callable)}
     * 使用 Function 简化编码
     *
     * @param key         k
     * @param cacheLoader l
     * @param <KEY>       k type
     * @param <VALUE>     value type
     * @return old value
     */
    @Nullable
    default <KEY, VALUE> VALUE get(@Nonnull KEY key, Function<KEY, VALUE> cacheLoader) {
        return get(key, () -> cacheLoader.apply(key));
    }

    /**
     * 与 getValue 类似
     *
     * @param key   k
     * @param value v
     * @return old
     */
    @Nullable
    @SuppressWarnings("unchecked")
    default <T> T putOnAbsent(@Nonnull Object key, @Nullable Object value) {
        ValueWrapper existingValue = get(key);
        if (existingValue == null) {
            put(key, value);
        }
        Object result;
        return existingValue == null ? null : (result = existingValue.get()) == NullValue.INSTANCE ? null : (T) result;
    }

    /**
     * 获取多个值
     * 实现类可以通过并行手段优化，注意控制数量，不要单次太多即可
     *
     * @param keys k，有序
     * @param <T>  t
     * @return 返回的 list 可能包含null
     */
    default <T> List<T> getMulti(@NonNull List<? extends Serializable> keys) {
        if (CollectionUtils.isNotEmpty(keys)) {
            return keys.stream()
                    .map(k -> (T) getValue(k))
                    //.filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 返回 map
     *
     * @param keys k
     * @param <K>  k
     * @param <V>  v
     * @return k-v Map
     */
    default <K extends Serializable, V> Map<K, V> getMultiMap(@NonNull List<K> keys) {
        if (CollectionUtils.isNotEmpty(keys)) {
            ConcurrentMap<K, V> result = new ConcurrentHashMap<>(keys.size());
            for (K k : keys) {
                V v = getValue(k);
                if (v != null) {
                    result.put(k, v);
                }
            }
            return result;
        }
        return Collections.emptyMap();
    }

    /**
     * 删除多个
     * 实现类可以通过并行手段优化，注意控制数量，不要单次太多即可
     *
     * @param keys k
     */
    default void evictMulti(@NonNull Collection<? extends Serializable> keys) {
        if (CollectionUtils.isNotEmpty(keys)) {
            for (Object k : keys) {
                evict(k);
            }
        }
    }

}
