package org.shoulder.batch.endpoint;

import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    private final BatchService batchService;

    /**
     * 导出
     */
    private final ExportService exportService;

    /**
     * 批量记录查询
     */
    private final RecordService recordService;

    public ImportController(BatchService batchService, ExportService exportService, RecordService recordService) {
        this.batchService = batchService;
        this.exportService = exportService;
        this.recordService = recordService;
    }

    /**
     * 实现举例：上传数据导入文件
     */
    @Override
    public BaseResult<String> doValidate(MultipartFile file, String charsetLanguage) throws Exception {
        // 示例：保存文件，解析文件，然后校验，返回校验任务标识
//        batchService.doProcess()
//        try {
//            InputStreamReader in = new InputStreamReader(file.getInputStream(), AppInfo.charset());
//            CsvReader reader = new CsvReader(in);
//            List<String[]> allLines = reader.readAll();
//            int ignore = getIgnoreRows(allLines);
//            //校验文件
//            String checkCSVResult = checkCSV(allLines, ignore);
//            if(StringUtils.isNotEmpty(checkCSVResult))
//            {
//                return new ActionResult(com.hikvision.modules.common.constant.ConstParamErrorCode.SYSTEM_CODE_FAIL +
//                                        "", checkCSVResult);
//            }
//            //初始化进度
//            BatchRecordCollection.put(uuid,new ImportOperateRecordDTO().setStartTime(System.currentTimeMillis()));
//            final String saveUuid = uuid;
//            threadExecutor.execute(()-> {
//                try{
//                    saveImportResult(allLines,ignore,saveUuid);
//                }catch (Exception e){
//                    LOGGER.errorWithErrorCode(ConstParamErrorCode.BS_SYSTEM_ERROR.code(), "startVerifyImport error", e);
//                }
//            });
//        }catch (Exception e) {
//            result = new ActionResult(com.hikvision.modules.common.constant.ConstParamErrorCode.SYSTEM_CODE_FAIL + "", "文件内容错误");
//        }finally {
//            ImportOperateRecordDTO importOperateRecordDTO = BatchRecordCollection.get(uuid);
//            if(importOperateRecordDTO != null)
//            {
//                importOperateRecordDTO.end();
//            }
//        }
//        result.setData(uuid);

        String taskId = "doValidate";
        return BaseResult.success(taskId);
    }


    /**
     * 实现举例：批量导入
     */
    @Override
    public BaseResult<String> doExecute(@RequestBody ExecuteOperationParam executeOperationParam) {
        // 示例：从缓存中拿出校验结果，根据校验结果组装为 BatchData，执行导入
        String taskId = executeOperationParam.getTaskId();
        BatchProgressRecord process = batchService.queryBatchProgress(taskId);
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
