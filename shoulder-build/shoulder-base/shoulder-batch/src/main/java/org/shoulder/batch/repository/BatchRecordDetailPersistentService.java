package org.shoulder.batch.repository;

import org.shoulder.batch.enums.BatchDetailResultStatusEnum;
import org.shoulder.batch.model.BatchRecordDetail;

import java.util.List;

/**
 * 批处理记录持久化接口
 *
 * @author lym
 */
public interface BatchRecordDetailPersistentService {

    /**
     * 批量新增处理详情
     *
     * @param batchRecordDetailList 要插入的记录
     */
    void batchSave(String recordId, List<BatchRecordDetail> batchRecordDetailList);

    /**
     * 查询所有的批量处理记录
     *
     * @param recordId   记录标识
     * @param resultList 结果状态
     * @return 所有的批量处理记录
     */
    default List<BatchRecordDetail> findAllByRecordIdAndStatus(String recordId, List<BatchDetailResultStatusEnum> resultList) {
        return findAllByRecordIdAndStatusAndIndex(recordId, resultList, null, null);
    }

    /**
     * 查询所有的批量处理记录
     *
     * @param recordId   记录标识
     * @param resultList 结果状态
     * @param indexStart 希望查询的第一个分片
     * @param indexEnd 希望查询的最后一个分片
     * @return 所有的批量处理记录
     */
    List<BatchRecordDetail> findAllByRecordIdAndStatusAndIndex(String recordId, List<BatchDetailResultStatusEnum> resultList, Integer indexStart, Integer indexEnd);

    /**
     * 查询所有的批量处理记录
     *
     * @param recordId   记录标识
     * @return 所有的批量处理记录
     */
    List<BatchRecordDetail> findAllByRecordId(String recordId);

}
