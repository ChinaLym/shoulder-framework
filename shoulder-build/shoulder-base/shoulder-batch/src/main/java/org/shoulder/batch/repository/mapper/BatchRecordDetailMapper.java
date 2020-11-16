package org.shoulder.batch.repository.mapper;

import org.shoulder.batch.model.BatchRecordDetail;

import java.util.List;

/**
 * 批量处理记录详情 Mapper
 *
 * @author lym
 */
public interface BatchRecordDetailMapper {


    /**
     * 批量新增处理详情
     *
     * @param batchRecordDetailList 要插入的记录
     */
    void batchInsertRecordDetail(List<BatchRecordDetail> batchRecordDetailList);

    /**
     * 查询所有的批量处理记录
     *
     * @param recordId 记录标识
     * @param resultList   结果状态
     * @return 所有的批量处理记录
     */
    List<BatchRecordDetail> findAllByResult(String recordId, List<Integer> resultList);

}
