package org.shoulder.batch.repository;

import org.shoulder.batch.repository.po.BatchRecordPO;

import java.util.List;
import java.util.Map;

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
    void insert(BatchRecordPO record);

    /**
     * 根据 任务标识 获取批处理记录
     *
     * @param importId 主键
     * @return 记录
     */
    BatchRecordPO findById(String importId);

    /**
     * 根据条件分页查询批处理记录
     * todo 【可读】使用 DTO 代替 Map
     *
     * @param condition tableName, userCode
     * @return 查询结果
     */
    List<BatchRecordPO> findByPage(Map<String, Object> condition);

    /**
     * 根据用户编码查询最近批处理的记录
     *
     * @param condition tableName, userCode, flag
     * @return 上次批量处理记录
     */
    BatchRecordPO findLast(Map<String, Object> condition);

}
