package org.shoulder.batch.repository.mapper;

import org.shoulder.batch.model.BatchRecordDetail;

import java.util.List;

/**
 * 批量执行记录详情 Mapper
 *
 * @author lym
 */
public interface ImportRecordDetailMapper {


    /**
     * 批量导入导入详情
     *
     * @param batchRecordDetailList 要插入的记录
     */
    void batchInsertRecordDetail(List<BatchRecordDetail> batchRecordDetailList);

    /**
     * 查询所有的批量执行记录
     *
     * @param recordId 记录标识
     * @param result   结果状态
     * @return 所有的批量执行记录
     */
    List<BatchRecordDetail> findAllByResult(String recordId, String result);

}
