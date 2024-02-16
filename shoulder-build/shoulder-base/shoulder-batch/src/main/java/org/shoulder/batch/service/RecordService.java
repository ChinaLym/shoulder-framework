package org.shoulder.batch.service;

import org.shoulder.batch.enums.ProcessStatusEnum;
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
     * 分页获取批处理详情列表，用于批处理完毕查看结果以及将结果导出
     *
     * @param batchId 批处理任务id
     * @return List<ImportRecordDetail>
     */
    List<BatchRecordDetail> findAllRecordDetail(String batchId);

    /**
     * 分页获取需要的结果数据
     *
     * @param batchId 批处理任务id
     * @param results 记录类型
     * @return List<ImportRecordDetail>
     */
    default List<BatchRecordDetail> findRecordDetailsByResults(String batchId, ProcessStatusEnum... results) {
        return findRecordDetailsByResults(batchId, Arrays.stream(results).collect(Collectors.toList()));
    }

    /**
     * 分页获取需要的结果数据
     *
     * @param batchId 批处理任务id
     * @param results 需过滤的处理结果类型、若为空 / null 则查询全部
     * @return 批处理详情
     */
    List<BatchRecordDetail> findRecordDetailsByResults(String batchId, List<ProcessStatusEnum> results);

}
