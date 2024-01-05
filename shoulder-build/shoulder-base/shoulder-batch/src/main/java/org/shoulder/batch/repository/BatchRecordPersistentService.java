package org.shoulder.batch.repository;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.shoulder.batch.model.BatchRecord;

import java.util.List;

/**
 * 批量处理记录mapper
 *
 * @author lym
 */
public interface BatchRecordPersistentService {

    /**
     * 单条插入
     *
     * @param record 批量处理记录
     */
    void insert(@Nonnull BatchRecord record);

    /**
     * 根据 任务标识 获取批处理记录
     *
     * @param recordId 主键
     * @return 记录
     */
    @Nullable
    BatchRecord findById(@Nonnull String recordId);

    // ================================== 条件查询 ==========================================

    /**
     * 根据条件分页查询批处理记录
     *
     * @param dataType        查询条件
     * @param pageNum         查询条件
     * @param pageSize        查询条件
     * @param currentUserCode 查询条件
     * @return 查询结果
     */
    @Nonnull
    List<BatchRecord> findByPage(@Nonnull String dataType, Integer pageNum, Integer pageSize, String currentUserCode);

    /**
     * 根据用户编码查询最近批处理的记录
     *
     * @param currentUserCode tableName, userCode, flag
     * @return 上次批量处理记录
     */
    @Nullable
    BatchRecord findLast(@Nonnull String dataType, String currentUserCode);

}
