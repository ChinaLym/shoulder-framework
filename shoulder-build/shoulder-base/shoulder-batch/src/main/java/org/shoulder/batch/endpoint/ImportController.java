package org.shoulder.batch.endpoint;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.batch.dto.param.ExecuteOperationParam;
import org.shoulder.batch.dto.param.QueryImportResultDetailParam;
import org.shoulder.batch.dto.result.BatchProcessResult;
import org.shoulder.batch.enums.BatchResultEnum;
import org.shoulder.batch.enums.ExportConstants;
import org.shoulder.batch.model.BatchProgress;
import org.shoulder.batch.service.BatchService;
import org.shoulder.batch.service.ExportService;
import org.shoulder.batch.service.RecordService;
import org.shoulder.core.dto.response.PageResult;
import org.shoulder.core.dto.response.RestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 导入相关
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
     * 上传数据导入文件
     */
    @Override
    public RestResult<String> doValidate(MultipartFile file, String charsetLanguage) throws Exception {
        // todo 解析文件，然后校验，返回校验任务标识
        return batchService.doProcess(file);
    }


    /**
     * 批量导入
     */
    @Override
    public RestResult<String> doImport(@RequestBody ExecuteOperationParam executeOperationParam) {
        // todo 从缓存中拿出来校验结果，然后组装，执行导入
        return RestResult.success(
            batchService.doProcess(executeOperationParam)
        );
    }

    /**
     * 查询数据导入进度
     */
    @Override
    public RestResult<BatchProcessResult> queryOperationProcess(String taskId) {
        BatchProgress process = batchService.queryBatchProgress(taskId);
        //todo 转换
        return RestResult.success(process);
    }


    /**
     * 查询数据导入记录
     */
    @Override
    public RestResult<PageResult<BatchProcessResult>> queryImportRecord() {
        return RestResult.success(recordService.findLastRecord());
    }

    /**
     * 查询某次处理记录详情
     */
    @Override
    public RestResult<BatchProcessResult> queryImportRecordDetail(
        @RequestBody QueryImportResultDetailParam condition) {
        //todo 转换
        return RestResult.success(recordService.findAllRecordDetail(condition.getTaskId()));
    }


    /**
     * 数据导入模板下载
     * todo 导出 导入模板
     */
    @Override
    public void exportImportTemplate(HttpServletResponse response, String businessType) throws IOException {
        exportService.export(response.getOutputStream(), ExportConstants.CSV, Collections.emptyList(), businessType);

    }

    /**
     * 数据导入记录详情导出
     */
    @Override
    public void exportRecordDetail(HttpServletResponse response, QueryImportResultDetailParam condition) throws IOException {
        exportService.exportBatchDetail(response.getOutputStream(), ExportConstants.CSV, condition.getBusinessType(),
            condition.getTaskId(), CollectionUtils.emptyIfNull(condition.getStatusList())
                .stream().map(BatchResultEnum::of).collect(Collectors.toList()));
    }

    /**
     * 导出数据
     */
    //@Override
    public void export(HttpServletResponse response, String businessType) throws IOException {
        exportService.export();
    }


}
