package org.shoulder.batch.service;

import org.shoulder.batch.enums.BatchResultEnum;
import org.shoulder.batch.model.BatchRecord;
import org.shoulder.batch.model.BatchRecordDetail;
import org.shoulder.core.dto.response.PageResult;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 批量处理记录
 *
 * @author lym
 */
public interface RecordService {


    /**
     * 根据任务标识获取批量处理记录详情，用于处理完毕查看结果以及将处理结果导出
     *
     * @param importCode 任务标识
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
     * @param dataType       数据类型，操作类型
     * @param currentUserName 当前用户
     * @return ImportRecord
     */
    BatchRecord findLastRecord(String dataType, String currentUserName);

    /**
     * 分页获取批处理详情列表，用于批处理完毕查看结果以及将结果导出
     *
     * @param taskId 任务标识
     * @return List<ImportRecordDetail>
     */
    List<BatchRecordDetail> findAllRecordDetail(String taskId);

    /**
     * 分页获取需要的结果数据
     *
     * @param taskId    任务标识
     * @param results 记录类型
     * @return List<ImportRecordDetail>
     */
    default List<BatchRecordDetail> findRecordDetailsByResults(String taskId, BatchResultEnum... results) {
        return findRecordDetailsByResults(taskId, Arrays.stream(results).collect(Collectors.toList()));
    }

    /**
     * 分页获取需要的结果数据
     *
     * @param taskId 任务标识
     * @param results     需过滤的处理结果类型、若为空 / null 则查询全部
     * @return 批处理详情
     */
    List<BatchRecordDetail> findRecordDetailsByResults(String taskId, List<BatchResultEnum> results);


}
