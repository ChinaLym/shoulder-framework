package org.shoulder.batch.endpoint;

import cn.hutool.core.io.IoUtil;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.batch.constant.BatchConstants;
import org.shoulder.batch.dto.param.AdvanceBatchParam;
import org.shoulder.batch.dto.param.QueryImportResultDetailParam;
import org.shoulder.batch.dto.result.BatchProcessResult;
import org.shoulder.batch.dto.result.BatchRecordResult;
import org.shoulder.batch.enums.BatchErrorCodeEnum;
import org.shoulder.batch.enums.ProcessStatusEnum;
import org.shoulder.batch.model.BatchData;
import org.shoulder.batch.model.BatchRecord;
import org.shoulder.batch.model.BatchRecordDetail;
import org.shoulder.batch.progress.BatchProgressRecord;
import org.shoulder.batch.service.BatchService;
import org.shoulder.batch.service.ExportService;
import org.shoulder.batch.service.RecordService;
import org.shoulder.batch.spi.BatchImportDataItem;
import org.shoulder.batch.spi.DataItem;
import org.shoulder.batch.spi.csv.DataItemConvertFactory;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.converter.ShoulderConversionService;
import org.shoulder.core.dto.request.PageQuery;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.annotation.OperationLog.Operations;
import org.shoulder.log.operation.context.OpLogContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
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

    /**
     * csv 导入转换工厂
     */
    private final DataItemConvertFactory dataItemConvertFactory;

    private final ShoulderConversionService conversionService;

    public ImportController(BatchService batchService, ExportService exportService, RecordService recordService,
                            DataItemConvertFactory dataItemConvertFactory, ShoulderConversionService shoulderConversionService) {
        this.batchService = batchService;
        this.exportService = exportService;
        this.recordService = recordService;
        this.dataItemConvertFactory = dataItemConvertFactory;
        this.conversionService = shoulderConversionService;
    }

    /**
     * 实现举例：上传数据导入文件
     */
    @Override
    @OperationLog(operation = Operations.UPLOAD_AND_VALIDATE)
    public BaseResult<String> validate(String businessType, MultipartFile file,
                                       String charsetLanguage) throws Exception {
        // todo 文件 > 10M Error; > 1M persistent and validate; > 100kb;
        // 暂时只支持 csv
        AssertUtils.isTrue(file.getOriginalFilename().endsWith(".csv"), BatchErrorCodeEnum.CSV_HEADER_ERROR);

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

        OpLogContextHolder.getLog().setExtField("size", recordList.size());
        //校验文件：校验文件头未改变
        checkImportCsvHeader(businessType, recordList);

        BatchData batchData = new BatchData();
        batchData.setDataType(businessType);
        batchData.setOperation(Operations.UPLOAD_AND_VALIDATE);
        batchData.setBatchListMap(new HashMap<>());
        batchData.getBatchListMap().put(Operations.UPLOAD_AND_VALIDATE, convertToDataItemList(businessType, recordList));

        // 保存文件，解析文件，然后校验，返回校验批处理任务id
        String batchId = batchService.doProcess(batchData);

        return BaseResult.success(batchId);
    }

    private List<? extends DataItem> convertToDataItemList(String businessType, List<Record> recordList) {
        AssertUtils.notNull(dataItemConvertFactory, CommonErrorCodeEnum.CODING, "no bean: dataItemConvertFactory");
        return dataItemConvertFactory.convertRecordToDataItem(businessType, recordList);
    }

    private void checkImportCsvHeader(String businessType, List<Record> recordList) {
        // todo 获取文件头，然后对比文件头未被篡改

        AssertUtils.notNull(businessType, BatchErrorCodeEnum.CSV_HEADER_ERROR);
    }

    /**
     * 实现举例：批量导入
     */
    @Override
    @OperationLog(operation = Operations.IMPORT)
    public BaseResult<String> advance(AdvanceBatchParam advanceBatchParam) {
        // 从缓存中拿出校验结果，根据校验结果组装为 BatchData，执行导入
        String batchId = advanceBatchParam.getBatchId();
        BatchProgressRecord process = batchService.queryBatchProgress(batchId);
        AssertUtils.isTrue(process.hasFinish(), BatchErrorCodeEnum.TASK_STATUS_ERROR);
        // 从数据库查寻 todo lock 避免重复导入
        BatchRecord record = recordService.findRecordById(advanceBatchParam.getBatchId());
        AssertUtils.notNull(record, CommonErrorCodeEnum.DATA_NOT_EXISTS);
        AssertUtils.equals(String.valueOf(record.getCreator()), AppContext.getUserId(), CommonErrorCodeEnum.PERMISSION_DENY);
        AssertUtils.equals(record.getDataType(), advanceBatchParam.getDataType(), CommonErrorCodeEnum.ILLEGAL_PARAM);
        AssertUtils.equals(record.getOperation(), advanceBatchParam.getCurrentOperation(), CommonErrorCodeEnum.ILLEGAL_PARAM);
        // todo 读配置，校验参数与配置一致
        AssertUtils.equals(Operations.IMPORT, advanceBatchParam.getNextOperation(), CommonErrorCodeEnum.ILLEGAL_PARAM);
        final int total = record.getTotalNum();

        BatchData batchData = new BatchData();
        batchData.setOperation(advanceBatchParam.getNextOperation());
        batchData.setDataType(advanceBatchParam.getDataType());
        batchData.setBatchListMap(new HashMap<>());
        List<BatchImportDataItem> importDataItemList = new ArrayList<>();
        importDataItemList.add(new BatchImportDataItem(
            total, 200, batchId, Map.of(BatchImportDataItem.EXT_KEY_UPDATE_REPEAT, advanceBatchParam.getUpdateRepeat())));
        batchData.getBatchListMap().put(advanceBatchParam.getNextOperation(), importDataItemList);

        String nextStageId = batchService.doProcess(batchData);
        return BaseResult.success(nextStageId);
    }

    /**
     * 查询数据导入进度
     */
    @Override
    public BaseResult<BatchProcessResult> queryProcess(String batchId) {
        BatchProgressRecord process = batchService.queryBatchProgress(batchId);
        return BaseResult.success(conversionService.convert(process, BatchProcessResult.class));
    }

    /**
     * 查询数据导入记录
     */
    @Override
    public BaseResult<ListResult<BatchRecordResult>> pageQueryImportRecord(String dataType) {
        return BaseResult.success(
            // todo admin 可以不带 userId
            Stream.of(recordService.findLastRecord(dataType, AppContext.getUserId()))
                .map(r -> conversionService.convert(r, BatchRecordResult.class))
                .collect(Collectors.toList()
                )
        );
    }

    /**
     * 查询某次处理记录详情
     */
    @Override
    public BaseResult<BatchRecordResult> pageQueryImportRecordDetail(
        QueryImportResultDetailParam condition) {
        BatchRecord record = recordService.findRecordById(condition.getBatchId());
        AssertUtils.notNull(record, CommonErrorCodeEnum.DATA_NOT_EXISTS);
        List<BatchRecordDetail> details = recordService.findAllDetailByRecordIdAndStatusAndIndex(record.getId(),
            null, condition.getPageNo(), condition.getPageSize());
        record.setDetailList(details);
        BatchRecordResult result = conversionService.convert(record, BatchRecordResult.class);
        return BaseResult.success(result);
    }

    /**
     * 数据导入模板下载
     * 示例： 导出 导入数据模板
     *
     * @return
     */
    @Override
    public void exportImportTemplate(HttpServletResponse response, String businessType) throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        exportService.export(byteArrayOutputStream, BatchConstants.CSV, Collections.emptyList(), businessType);

        if (byteArrayOutputStream.size() == 0) {
            response.setStatus(404);
            AssertUtils.notEquals(byteArrayOutputStream.size(), 0, CommonErrorCodeEnum.FILE_READ_FAIL);
            //return ResponseEntity.notFound().build();
        }

        byte[] templateBytes = byteArrayOutputStream.toByteArray();
        // 先设置 header 再写 responseStream，否则header会失效
        String fileName = URLEncoder.encode(businessType + "-import-template.csv", AppInfo.charset());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setCharacterEncoding(AppInfo.charset().name());
        response.setContentLength(templateBytes.length);
        // 创建输入流以读取文件
        IoUtil.copy(new ByteArrayInputStream(templateBytes), response.getOutputStream());
        // 设置响应头信息
        //InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(templateBytes));
        //HttpHeaders headers = new HttpHeaders();
        //headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + businessType + "-import-template.csv\"");
        //headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        // 构建响应实体
        //return ResponseEntity.ok()
        //    .headers(headers)
        //    .contentLength(templateBytes.length)
        //    .body(resource);
    }

    /**
     * 数据导入记录详情导出
     */
    @Override
    public void exportRecordDetail(HttpServletResponse response, QueryImportResultDetailParam condition) throws IOException {
        BatchRecord recordInDb = recordService.findRecordById(condition.getBatchId());
        AssertUtils.notNull(recordInDb, CommonErrorCodeEnum.DATA_NOT_EXISTS);
        AssertUtils.equals(recordInDb.getDataType(), condition.getBusinessType(), CommonErrorCodeEnum.ILLEGAL_PARAM);
        int total = recordInDb.getTotalNum();
        // 根据 是否 admin，决定 totalNumber 是否可以导入，否则最多xx条，如果太多可以考虑导出压缩文件

        exportService.exportBatchDetail(response.getOutputStream(), BatchConstants.CSV,
            recordInDb.getDataType(),
            recordInDb.getId(),
            CollectionUtils.emptyIfNull(condition.getStatusList())
                .stream().map(ProcessStatusEnum::of).collect(Collectors.toList())
        );
    }

    /**
     * 导出数据
     */
    @Override
    public void export(HttpServletResponse response, String businessType,
                       @RequestBody PageQuery<Map> exportCondition) throws IOException {
        List<Supplier<List<Map<String, String>>>> exportData = new ArrayList<>();
        exportService.export(response.getOutputStream(), businessType, exportData, "exportTemplateId");
    }

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
