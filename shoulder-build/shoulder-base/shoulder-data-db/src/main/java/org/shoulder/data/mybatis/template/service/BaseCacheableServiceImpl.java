package org.shoulder.data.mybatis.template.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.session.SqlSession;
import org.shoulder.core.cache.Cache;
import org.shoulder.core.cache.CacheDecorate;
import org.shoulder.data.mybatis.template.dao.BaseMapper;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 带缓存的 Service
 *
 * @param <MAPPER>
 * @param <ENTITY>
 * @author lym
 */
public abstract class BaseCacheableServiceImpl<MAPPER extends BaseMapper<ENTITY>,
        ENTITY extends BaseEntity<? extends Serializable>>
        extends BaseServiceImpl<MAPPER, ENTITY>
        implements BaseCacheableService<ENTITY>,
        ApplicationListener<ApplicationStartedEvent> {

    // 选择特定的 Cache，便于管理 key格式 / 缓存有效时长

    protected Cache cache;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        // 获取cache 入股没有 cache则降级缓存
        try {
            cache = event.getApplicationContext().getBean(Cache.class);
        } catch (Exception e) {
            cache = new CacheDecorate(new ConcurrentMapCache("cacheService_" + getClass().getName()));
            logger.warn("No cache bean, fail back to memory: " + getClass().getName());
        }
    }

    /**
     * 一次查多个 key 时，最多多少个
     */
    protected static final int MAX_BATCH_KEY_SIZE = 20;

    /**
     * 缓存key 构造器
     *
     * @param keywords 通常为 id
     * @return 缓存key构造器
     */
    protected String generateCacheKey(Serializable keywords) {
        return getEntityClass().getSimpleName() + ":" + keywords;
    }

    protected String generateCacheKey(Object... keywords) {

        StringBuilder cacheKey = new StringBuilder(getEntityClass().getSimpleName());
        for (Object keyword : keywords) {
            cacheKey.append(":");
            cacheKey.append(keyword.toString());
        }
        return cacheKey.toString();
    }
    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    /**
     * 查缓存，miss 则从db加载
     *
     * @param id 主键
     * @return v
     */
    @Override
    @Transactional(readOnly = true)
    public ENTITY getByIdFromCache(Serializable id) {
        Object cacheKey = generateCacheKey(id);
        return cache.get(cacheKey, () -> super.getById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ENTITY> loadByIds(@NonNull Collection<? extends Serializable> ids, Function<Collection<? extends Serializable>, Collection<ENTITY>> loader) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        // 拼接keys
        List<? extends Serializable> keys = ids.stream().map(this::generateCacheKey).collect(Collectors.toList());

        // 返回的是缓存中存在的数据
        List<ENTITY> valueList = cache.getMulti(keys);

        // 所有的key
        List<Serializable> keysList = Lists.newArrayList(ids);
        // 缓存不存在的key
        Set<Serializable> missedKeys = Sets.newLinkedHashSet();

        List<ENTITY> allList = new ArrayList<>();
        for (int i = 0; i < valueList.size(); i++) {
            ENTITY v = valueList.get(i);
            Serializable k = keysList.get(i);
            if (v == null) {
                missedKeys.add(k);
            } else {
                allList.add(v);
            }
        }
        // 加载miss 的数据，并设置到缓存
        if (CollUtil.isNotEmpty(missedKeys)) {
            if (loader == null) {
                loader = this::listByIds;
            }
            Collection<ENTITY> missList = loader.apply(missedKeys);
            missList.forEach(this::buildCache);
            allList.addAll(missList);
        }
        return allList;
    }

    @Override
    @Transactional(readOnly = true)
    public ENTITY getByCacheKey(Object key, Function<Object, Object> loader) {
        Object id = cache.get(key, loader);
        return id == null ? null : getByIdFromCache(Convert.toLong(id));
    }


    /**
     * 删除后，逐出缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        boolean bool = super.removeById(id);
        evictCache(id);
        return bool;
    }

    /**
     * 删除后，逐出缓存
     */
    @Override
    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<?> idList) {
        if (CollUtil.isEmpty(idList)) {
            return true;
        }
        boolean flag = super.removeByIds(idList);

        evictCache((Collection<? extends Serializable>) idList);
        return flag;
    }

    /**
     * 修改后，逐出缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(ENTITY model) {
        boolean save = super.save(model);
        buildCache(model);
        return save;
    }

    /**
     * 修改后，逐出缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAllById(ENTITY model) {
        boolean updateBool = super.updateAllById(model);
        evictCache(model);
        return updateBool;
    }

    /**
     * 修改后，逐出缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(ENTITY model) {
        boolean updateBool = super.updateById(model);
        evictCache(model);
        return updateBool;
    }


    /**
     * 修改后，逐出缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatch(Collection<ENTITY> entityList, int batchSize) {
        String sqlStatement = getSqlStatement(SqlMethod.INSERT_ONE);
        return executeBatch(entityList, batchSize, (sqlSession, entity) -> {
            sqlSession.insert(sqlStatement, entity);

            // 设置缓存
            buildCache(entity);
        });
    }

    /**
     * 修改后，逐出缓存
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveOrUpdateBatch(Collection<ENTITY> entityList, int batchSize) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(getEntityClass());
        Assert.notNull(tableInfo, "error: can not execute. because can not find cache of TableInfo for entity!");
        String keyProperty = tableInfo.getKeyProperty();
        Assert.notEmpty(keyProperty, "error: can not execute. because can not find column for id from entity!");

        BiPredicate<SqlSession, ENTITY> predicate = (sqlSession, entity) -> {
            Object idVal = ReflectionKit.getFieldValue(entity, keyProperty);
            return StringUtils.checkValNull(idVal)
                    || CollectionUtils.isEmpty(sqlSession.selectList(getSqlStatement(SqlMethod.SELECT_BY_ID), entity));
        };

        BiConsumer<SqlSession, ENTITY> consumer = (sqlSession, entity) -> {
            MapperMethod.ParamMap<ENTITY> param = new MapperMethod.ParamMap<>();
            param.put(Constants.ENTITY, entity);
            sqlSession.update(getSqlStatement(SqlMethod.UPDATE_BY_ID), param);

            // 清理缓存
            evictCache(entity);
        };

        String sqlStatement = SqlHelper.getSqlStatement(this.mapperClass, SqlMethod.INSERT_ONE);
        return SqlHelper.executeBatch(getEntityClass(), log, entityList, batchSize, (sqlSession, entity) -> {
            if (predicate.test(sqlSession, entity)) {
                sqlSession.insert(sqlStatement, entity);
                // 设置缓存
                buildCache(entity);
            } else {
                consumer.accept(sqlSession, entity);
            }
        });


    }

    /**
     * 修改后，逐出缓存
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateBatchById(Collection<ENTITY> entityList, int batchSize) {
        String sqlStatement = getSqlStatement(SqlMethod.UPDATE_BY_ID);
        return executeBatch(entityList, batchSize, (sqlSession, entity) -> {
            MapperMethod.ParamMap<ENTITY> param = new MapperMethod.ParamMap<>();
            param.put(Constants.ENTITY, entity);
            sqlSession.update(sqlStatement, param);

            // 清理缓存
            evictCache(entity);
        });
    }

    /**
     * 默认全量加载到缓存，量比较大，最好复写
     */
    @Override
    public void refreshCache() {
        list().forEach(this::buildCache);
    }

    @Override
    public void clearCache() {
        list().forEach(this::evictCache);
    }


    protected void evictCache(Serializable... ids) {
        evictCache(Arrays.asList(ids));
    }

    protected void evictCache(Collection<? extends Serializable> idList) {
        Collection<? extends Serializable> keys = idList.stream()
                .map(this::generateCacheKey)
                .collect(Collectors.toList());
        cache.evictMulti(keys);
    }

    protected void evictCache(ENTITY model) {
        Object id = fetchIdFromEntity(model);
        if (id != null) {
            Object key = generateCacheKey(id);
            cache.evict(key);
        }
    }

    protected void buildCache(ENTITY model) {
        Object id = fetchIdFromEntity(model);
        if (id != null) {
            Object key = generateCacheKey(id);
            cache.put(key, model);
        }
    }

    protected Object fetchIdFromEntity(ENTITY model) {
        return model == null ? null : model.getId();
    }

}
