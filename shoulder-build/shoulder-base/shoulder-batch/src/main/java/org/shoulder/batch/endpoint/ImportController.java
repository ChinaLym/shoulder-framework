package org.shoulder.batch.endpoint;

import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
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
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
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
    public BaseResult<String> doValidate(String businessType, MultipartFile file,
                                         String charsetLanguage) throws Exception {
        // todo 文件 > 10M Error; > 1M persistent and validate; > 100kb;


        CsvParserSettings settings = new CsvParserSettings();
        // 【支持定制】todo 通过 spring 配置设置
        settings.setFormat(new CsvFormat());
        settings.setNumberOfRecordsToRead(10000);
        // 忽略的注释行
        settings.setNumberOfRowsToSkip(2);
        CsvParser csvParser = new CsvParser(settings);

        List<Record> recordList = null;
        try (InputStreamReader in = new InputStreamReader(file.getInputStream(), AppInfo.charset())) {
            recordList = csvParser.parseAllRecords(in);
        }

        //校验文件：校验文件头未改变
        //boolean headerValid = checkImportCsvHeader(businessType, recordList);
        //AssertUtils.isTrue(headerValid, BatchErrorCodeEnum.CSV_HEADER_ERROR);
        //
        //BatchData batchData = new BatchData();
        //batchData.setDataType(businessType);
        //batchData.setOperation(Operation.IMPORT_VALIDATE);
        //batchData.setBatchListMap(Collections.singletonMap(Operation.IMPORT_VALIDATE, recordList));)
        //
        //// 保存文件，解析文件，然后校验，返回校验任务标识
        //batchService.doProcess();

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
     *
     * @return
     */
    @Override
    public ResponseEntity<Resource> exportImportTemplate(HttpServletResponse response, String businessType) throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        exportService.export(byteArrayOutputStream, BatchConstants.CSV, Collections.emptyList(), businessType);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + businessType + "-import-template.csv\"");
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        if (byteArrayOutputStream.size() == 0) {
            return ResponseEntity.notFound().build();
        }

        byte[] templateBytes = byteArrayOutputStream.toByteArray();
        // 创建输入流以读取文件
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(templateBytes));

        // 设置响应头信息
        HttpHeaders headers = new HttpHeaders();
        //URLEncoder.encode(fileName, AppInfo.charset())
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + businessType + "-import-template.csv\"");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setCharacterEncoding("utf-8");

        // 构建响应实体
        return ResponseEntity.ok()
            .headers(headers)
            .contentLength(templateBytes.length)
            .body(resource);
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
    //public void export(HttpServletResponse response, String businessType) throws IOException {
        //
        //exportService.export();
    //}

    /**
     * 给导出的文件命名
     *
     * @param response http 响应
     * @param fileName 导出文件名
     * @deprecated 不要在这里做
     */
    public void setExportFileName(HttpServletResponse response, String fileName, String encoding, long length) {
        response.setHeader("Content-Disposition", "attachment; filename=" +
                                                  URLEncoder.encode(fileName, AppInfo.charset()));
        response.setHeader("Content-Type", "application/octet-stream");
    }

}
