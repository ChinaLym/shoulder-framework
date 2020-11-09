package org.shoulder.batch.service;

import org.shoulder.batch.dto.ImportRecordDto;
import org.shoulder.batch.enums.ImportResultEnum;
import org.shoulder.batch.model.ImportRecord;
import org.shoulder.batch.model.ImportRecordDetail;
import org.shoulder.core.dto.response.PageResult;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 导入记录
 *
 * @author liuyanming
 */
public interface RecordService {


    /**
     * 根据导入标识获取导入记录详情，用于导入完毕查看结果以及将导入结果导出
     *
     * @param importCode 导入编码
     * @return ImportRecordDto
     * @throws Exception 异常
     */
    ImportRecord findRecordById(String importCode) throws Exception;

    /**
     * 分页获取导入列表（查询历史记录）
     *
     * @param tableName       导入类型
     * @param pageNum         页码
     * @param pageSize        页数
     * @param currentUserName 当前用户
     * @return PageBean
     * @throws Exception 异常
     */
    PageResult<ImportRecordDto> pageQueryRecord(String tableName, Integer pageNum, Integer pageSize,
                                                String currentUserName) throws Exception;

    /**
     * 获取某个用户的最后一次的导入记录
     *
     * @param tableName       导入类型
     * @param currentUserName 当前用户
     * @return ImportRecordDto
     * @throws Exception 异常
     */
    ImportRecordDto findLastRecord(String tableName, String currentUserName) throws Exception;

    /**
     * 分页获取导入列表，用于导入完毕查看结果以及将导入结果导出
     *
     * @param code 导入编码
     * @return List<ImportRecordDetail>
     */
    List<ImportRecordDetail> findAllImportDetail(String code);

    /**
     * 分页获取需要的结果数据
     *
     * @param code    导入编码
     * @param results 记录类型
     * @return List<ImportRecordDetail>
     */
    default List<ImportRecordDetail> findRecordDetailsByResults(String code, ImportResultEnum... results) {
        return findRecordDetailsByResults(code, Arrays.stream(results).collect(Collectors.toList()));
    }

    /**
     * 分页获取需要的结果数据
     *
     * @param processCode    导入编码
     * @param results 记录类型
     * @return List<ImportRecordDetail>
     */
    List<ImportRecordDetail> findRecordDetailsByResults(String processCode, List<ImportResultEnum> results);


}
