package org.shoulder.batch.endpoint;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.batch.constant.BatchConstants;
import org.shoulder.batch.dto.param.ExecuteOperationParam;
import org.shoulder.batch.dto.param.QueryImportResultDetailParam;
import org.shoulder.batch.dto.result.BatchProcessResult;
import org.shoulder.batch.dto.result.BatchRecordResult;
import org.shoulder.batch.enums.ProcessStatusEnum;
import org.shoulder.batch.model.BatchData;
import org.shoulder.batch.model.BatchProgressRecord;
import org.shoulder.batch.model.BatchRecord;
import org.shoulder.batch.model.BatchRecordDetail;
import org.shoulder.batch.model.convert.BatchModelConvert;
import org.shoulder.batch.service.BatchService;
import org.shoulder.batch.service.ExportService;
import org.shoulder.batch.service.RecordService;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 导入 API
 *
 * @author lym
 */
@Slf4j
@RestController
public class ImportController implements ImportRestfulApi {

    /**
     * 批量操作
     */
    @Autowired
    private BatchService batchService;

    /**
     * 导出
     */
    @Autowired
    private ExportService exportService;

    /**
     * 批量记录查询
     */
    @Autowired
    private RecordService recordService;


    /**
     * 实现举例：上传数据导入文件
     */
    @Override
    public BaseResult<String> doValidate(MultipartFile file, String charsetLanguage) throws Exception {
        // 示例：解析文件，然后校验，返回校验任务标识
        String taskId = "doValidate";
        return BaseResult.success(taskId);
    }


    /**
     * 实现举例：批量导入
     */
    @Override
    public BaseResult<String> doExecute(@RequestBody ExecuteOperationParam executeOperationParam) {
        // 示例：从缓存中拿出校验结果，根据校验结果组装为 BatchData，执行导入

        BatchData batchData = new BatchData();
        return BaseResult.success(
                batchService.doProcess(batchData)
        );
    }

    /**
     * 查询数据导入进度
     */
    @Override
    public BaseResult<BatchProcessResult> queryOperationProcess(String taskId) {
        BatchProgressRecord process = batchService.queryBatchProgress(taskId);
        return BaseResult.success(BatchModelConvert.CONVERT.toDTO(process));
    }


    /**
     * 查询数据导入记录
     */
    @Override
    public BaseResult<ListResult<BatchRecordResult>> queryImportRecord() {
        return BaseResult.success(
                Stream.of(recordService.findLastRecord("dataType", AppContext.getUserId()))
                        .map(BatchModelConvert.CONVERT::toDTO)
                        .collect(Collectors.toList()
                        )
        );
    }

    /**
     * 查询某次处理记录详情
     */
    @Override
    public BaseResult<BatchRecordResult> queryImportRecordDetail(
            @RequestBody QueryImportResultDetailParam condition) {
        BatchRecord record = recordService.findRecordById("xxx");
        List<BatchRecordDetail> details = recordService.findAllRecordDetail(condition.getTaskId());
        record.setDetailList(details);
        BatchRecordResult result = BatchModelConvert.CONVERT.toDTO(record);
        return BaseResult.success(result);
    }


    /**
     * 数据导入模板下载
     * 示例： 导出 导入数据模板
     */
    @Override
    public void exportImportTemplate(HttpServletResponse response, String businessType) throws IOException {
        exportService.export(response.getOutputStream(), BatchConstants.CSV, Collections.emptyList(), businessType);

    }

    /**
     * 数据导入记录详情导出
     */
    @Override
    public void exportRecordDetail(HttpServletResponse response, QueryImportResultDetailParam condition) throws IOException {
        exportService.exportBatchDetail(response.getOutputStream(), BatchConstants.CSV, condition.getBusinessType(),
                condition.getTaskId(), CollectionUtils.emptyIfNull(condition.getStatusList())
                        .stream().map(ProcessStatusEnum::of).collect(Collectors.toList()));
    }

    /**
     * 导出数据
     */
    //@Override
    public void export(HttpServletResponse response, String businessType) throws IOException {
        //exportService.export();
    }


}
