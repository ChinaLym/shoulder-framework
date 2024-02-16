package org.shoulder.batch.repository;

import jakarta.annotation.Nonnull;
import org.shoulder.batch.model.BatchRecord;
import org.springframework.cache.Cache;

import java.util.List;

/**
 * 批量处理记录mapper
 *
 * @author lym
 */
public class CacheBatchRecordPersistentServiceImpl implements BatchRecordPersistentService {

    /**
     * 缓存
     */
    private final Cache cache;

    public CacheBatchRecordPersistentServiceImpl(Cache cache) {
        this.cache = cache;
    }

    /**
     * 单条插入
     *
     * @param record 批量处理记录
     */
    @Override
    public void insert(@Nonnull BatchRecord record) {
        cache.put(record.getId(), record);
    }

    /**
     * 根据 批处理任务id 获取批处理记录
     *
     * @param recordId 主键
     * @return 记录
     */
    @Override
    public BatchRecord findById(@Nonnull String recordId) {
        Cache.ValueWrapper wrapper = cache.get(recordId);
        return wrapper == null ? null : (BatchRecord) wrapper.get();
    }

    /**
     * 根据条件分页查询批处理记录
     *
     * @param dataType        查询条件
     * @param pageNum         查询条件
     * @param pageSize        查询条件
     * @param currentUserCode 查询条件
     * @return 查询结果
     */
    @Nonnull @Override
    public List<BatchRecord> findByPage(@Nonnull String dataType, Integer pageNum, Integer pageSize,
                                        String currentUserCode) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " not support find by condition!");
    }

    /**
     * 根据用户编码查询最近批处理的记录
     *
     * @return 上次批量处理记录
     */
    @Override
    public BatchRecord findLast(@Nonnull String dataType, String currentUserCode) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " not support find by condition!");
    }

}
