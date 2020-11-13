package org.shoulder.batch.repository.mapper;

import org.shoulder.batch.model.BatchRecord;

import java.util.List;
import java.util.Map;

/**
 * 批量执行记录mapper
 *
 * @author lym
 */
public interface ImportRecordMapper {

    /**
     * 单条插入
     *
     * @param record 批量执行记录
     */
    void insert(BatchRecord record);

    /**
     * 根据导入ID获取导入信息
     *
     * @param importId 主键
     * @return 记录
     */
    BatchRecord findById(String importId);

    /**
     * 根据条件分页查询导入详情
     *
     * @param condition tableName, userCode
     * @return 查询结果
     */
    List<BatchRecord> findByPage(Map<String, Object> condition);

    /**
     * 根据用户编码查询最近导入的record
     *
     * @param condition tableName, userCode, flag
     * @return 上次批量执行记录
     */
    BatchRecord findLast(Map<String, Object> condition);

}
