package org.shoulder.batch.repository;

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
    void insert(BatchRecord record);

    /**
     * 根据 任务标识 获取批处理记录
     *
     * @param importId 主键
     * @return 记录
     */
    BatchRecord findById(String importId);

    /**
     * 根据条件分页查询批处理记录
     *
     * @param dataType        查询条件
     * @param pageNum         查询条件
     * @param pageSize        查询条件
     * @param currentUserCode 查询条件
     * @return 查询结果
     */
    List<BatchRecord> findByPage(String dataType, Integer pageNum, Integer pageSize, String currentUserCode);

    /**
     * 根据用户编码查询最近批处理的记录
     *
     * @param currentUserCode tableName, userCode, flag
     * @return 上次批量处理记录
     */
    BatchRecord findLast(String dataType, String currentUserCode);

}
