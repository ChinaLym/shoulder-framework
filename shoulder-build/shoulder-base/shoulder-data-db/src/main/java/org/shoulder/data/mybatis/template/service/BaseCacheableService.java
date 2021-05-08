package org.shoulder.data.mybatis.template.service;

import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * 扩展了缓存能力
 *
 * @param <T> 实体
 * @author lym
 */
public interface BaseCacheableService<T> extends BaseService<T> {

    /**
     * 先查缓存，再查db
     *
     * @param id 主键
     * @return 对象
     */
    T getByIdFromCache(Serializable id);

    /**
     * 根据 key 查询缓存中存放的id
     * 缓存不存在则根据loader加载并写入数据
     *
     * @param key    缓存key
     * @param loader 数据加载器
     * @return 对象
     */
    T getByCacheKey(Object key, Function<Object, Object> loader);

    /**
     * 可能会缓存穿透
     *
     * @param ids    主键id
     * @param loader 回调
     * @return 对象集合
     */
    List<T> loadByIds(@NonNull Collection<? extends Serializable> ids, Function<Collection<? extends Serializable>, Collection<T>> loader);

    /**
     * 刷新缓存
     */
    void refreshCache();

    /**
     * 清理缓存
     */
    void clearCache();
}
