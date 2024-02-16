package org.shoulder.batch.service;

import org.shoulder.batch.enums.ProcessStatusEnum;
import org.shoulder.batch.model.BatchRecord;
import org.shoulder.batch.model.BatchRecordDetail;
import org.shoulder.core.dto.response.PageResult;

import java.util.List;

/**
 * 批量处理记录
 *
 * @author lym
 */
public interface RecordService {

    /**
     * 根据批处理任务id获取批量处理记录详情，用于处理完毕查看结果以及将处理结果导出
     *
     * @param importCode 批处理任务id
     * @return ImportRecord
     */
    BatchRecord findRecordById(String importCode);

    /**
     * 分页获取处理列表（查询历史记录）
     *
     * @param dataType        数据类型
     * @param pageNum         页码
     * @param pageSize        页数
     * @param currentUserCode 当前用户
     * @return PageBean
     */
    PageResult<BatchRecord> pageQueryRecord(String dataType, Integer pageNum, Integer pageSize,
                                            String currentUserCode);

    /**
     * 获取某个用户的最后一次的批量处理记录
     *
     * @param dataType        数据类型，操作类型
     * @param currentUserName 当前用户
     * @return ImportRecord
     */
    BatchRecord findLastRecord(String dataType, String currentUserName);


    /**
     * 查询所有的批量处理记录
     *
     * @param recordId   记录标识
     * @param resultList 结果状态
     * @param indexStart 希望查询的第一个分片
     * @param indexEnd 希望查询的最后一个分片
     * @return 所有的批量处理记录
     */
    List<BatchRecordDetail> findAllDetailByRecordIdAndStatusAndIndex(String recordId, List<ProcessStatusEnum> resultList,
                                                                     Integer indexStart, Integer indexEnd);

}
