package org.shoulder.batch.endpoint;

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
import org.shoulder.batch.enums.BatchDetailResultStatusEnum;
import org.shoulder.batch.enums.BatchErrorCodeEnum;
import org.shoulder.batch.model.BatchData;
import org.shoulder.batch.model.BatchRecord;
import org.shoulder.batch.model.BatchRecordDetail;
import org.shoulder.batch.progress.BatchProgressRecord;
import org.shoulder.batch.service.BatchService;
import org.shoulder.batch.service.ExportService;
import org.shoulder.batch.service.RecordService;
import org.shoulder.batch.spi.BatchImportDataItem;
import org.shoulder.batch.spi.DataItem;
import org.shoulder.batch.spi.ExportDataQueryFactory;
import org.shoulder.batch.spi.csv.DataItemConvertFactory;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.converter.ShoulderConversionService;
import org.shoulder.core.dto.request.PageQuery;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.lock.ServerLock;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.annotation.OperationLog.Operations;
import org.shoulder.log.operation.context.OpLogContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
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
     * 校验结果有效期
     * 校验完毕后，需要在30min内执行导入，否则需要重新校验
     * 降低有人新增数据导致导入数据大量冲突而失败。
     */
    private static final Duration VALIDATE_RESULT_EXPIRATION = Duration.ofHours(1);


    private final ServerLock serverLock;

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

    private final List<ExportDataQueryFactory> exportDataQueryFactoryList;

    public ImportController(ServerLock serverLock, BatchService batchService, ExportService exportService, RecordService recordService,
                            DataItemConvertFactory dataItemConvertFactory, ShoulderConversionService shoulderConversionService,
                            List<ExportDataQueryFactory> exportDataQueryFactoryList) {
        this.serverLock = serverLock;
        this.batchService = batchService;
        this.exportService = exportService;
        this.recordService = recordService;
        this.dataItemConvertFactory = dataItemConvertFactory;
        this.conversionService = shoulderConversionService;
        this.exportDataQueryFactoryList = exportDataQueryFactoryList;
    }

    /**
     * 实现举例：上传数据导入文件
     */
    @Override
    @OperationLog(operation = Operations.UPLOAD_AND_VALIDATE)
    public BaseResult<String> validate(String businessType, MultipartFile file,
                                       String charsetLanguage) throws Exception {
        // todo 【进阶】文件 > 10M Error; > 1M persistent and validate; > 100kb;
        // 暂时只支持 csv
        AssertUtils.isTrue(file.getOriginalFilename().endsWith(".csv"), BatchErrorCodeEnum.CSV_HEADER_ERROR);

        CsvParserSettings settings = new CsvParserSettings();
        // todo 【进阶】通过 spring 配置设置
        settings.setFormat(new CsvFormat());
        settings.setNumberOfRecordsToRead(10000);
        // 忽略的注释行
        settings.setNumberOfRowsToSkip(0);
        CsvParser csvParser = new CsvParser(settings);

        List<Record> recordList;
        try (InputStreamReader in = new InputStreamReader(file.getInputStream(), AppInfo.charset())) {
            recordList = csvParser.parseAllRecords(in);
        }

        //校验文件：校验文件头未改变
        boolean valid = exportService.validateCsvHeader(businessType, recordList);
        AssertUtils.isTrue(valid, BatchErrorCodeEnum.CSV_HEADER_ERROR);

        OpLogContextHolder.getLog().setExtField("size", recordList.size());

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
        // 确保 record 存在
        BatchRecord record = recordService.findRecordById(advanceBatchParam.getBatchId());
        AssertUtils.notNull(record, CommonErrorCodeEnum.DATA_NOT_EXISTS);
        AssertUtils.equals(String.valueOf(record.getCreator()), AppContext.getUserId(), CommonErrorCodeEnum.PERMISSION_DENY);
        AssertUtils.equals(record.getDataType(), advanceBatchParam.getDataType(), CommonErrorCodeEnum.ILLEGAL_PARAM);
        AssertUtils.equals(record.getOperation(), advanceBatchParam.getCurrentOperation(), CommonErrorCodeEnum.ILLEGAL_PARAM);
        Instant validateTime = conversionService.convert(record.getCreateTime(), Instant.class);
        AssertUtils.isTrue(Instant.now().isBefore(validateTime.plus(VALIDATE_RESULT_EXPIRATION)), CommonErrorCodeEnum.ILLEGAL_STATUS);

        // todo 【进阶】读配置，校验参数与配置一致
        AssertUtils.equals(Operations.IMPORT, advanceBatchParam.getNextOperation(), CommonErrorCodeEnum.ILLEGAL_PARAM);
        OpLogContextHolder.getLog().setOperation(advanceBatchParam.getNextOperation());
        final int total = record.getTotalNum();

        BatchData batchData = new BatchData();
        batchData.setOperation(advanceBatchParam.getNextOperation());
        batchData.setDataType(advanceBatchParam.getDataType());
        batchData.setBatchListMap(new HashMap<>());
        List<BatchImportDataItem> importDataItemList = new ArrayList<>();
        importDataItemList.add(
                new BatchImportDataItem(total, 200, batchId,
                        Map.of(BatchImportDataItem.EXT_KEY_UPDATE_REPEAT, advanceBatchParam.getUpdateRepeat()))
        );
        batchData.getBatchListMap().put(advanceBatchParam.getNextOperation(), importDataItemList);

        // lock 避免重复导入，低频功能低内存，加长锁
        boolean locked = lockDefendRepeatAdvance(advanceBatchParam);
        AssertUtils.isTrue(locked, CommonErrorCodeEnum.REPEATED_SUBMIT);

        String nextStageId = batchService.doProcess(batchData);
        return BaseResult.success(nextStageId);
    }

    private boolean lockDefendRepeatAdvance(AdvanceBatchParam param) {
        // 不用关心谁持有，主打一个短时间防重复，长期可重试
        String resource = param.getDataType() + ":" + param.getBatchId() + ":"
                + param.getCurrentOperation() + ":" + param.getNextOperation();
        return serverLock.tryLock(resource, VALIDATE_RESULT_EXPIRATION);
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
                // admin 可以不带 userId 查所有
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
     */
    @Override
    public void exportImportTemplate(HttpServletResponse response, String businessType) throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String encoding = exportService.export(byteArrayOutputStream, BatchConstants.CSV, Collections.emptyList(), businessType);

        compositeResponse(response, businessType, byteArrayOutputStream, encoding);
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

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String encoding = exportService.exportBatchDetail(byteArrayOutputStream, BatchConstants.CSV,
                recordInDb.getDataType(),
                recordInDb.getId(),
                CollectionUtils.emptyIfNull(condition.getStatusList())
                        .stream().map(BatchDetailResultStatusEnum::of).collect(Collectors.toList())
        );
        compositeResponse(response, recordInDb.getDataType(), byteArrayOutputStream, encoding);
    }

    /**
     * 导出数据
     */
    @Override
    public void export(HttpServletResponse response, String businessType,
                       PageQuery<Map> exportCondition) throws IOException {
        // 找到数据查询构造器
        ExportDataQueryFactory exportDataQueryFactory = exportDataQueryFactoryList.stream()
                .filter(p -> p.support(businessType, exportCondition))
                .findFirst()
                .orElseThrow(() -> new BaseRuntimeException(CommonErrorCodeEnum.ILLEGAL_PARAM));
        List<Supplier<List<Map<String, String>>>> exportData = exportDataQueryFactory.createQuerySuppliers(businessType, exportCondition);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String encoding = exportService.export(byteArrayOutputStream, BatchConstants.CSV, exportData, businessType);
        compositeResponse(response, businessType, byteArrayOutputStream, encoding);
    }

    private static void compositeResponse(HttpServletResponse response, String businessType, ByteArrayOutputStream byteArrayOutputStream,
                                          String encoding) throws IOException {
        int length = byteArrayOutputStream.size();
        if (length == 0) {
            response.setStatus(404);
            AssertUtils.notEquals(byteArrayOutputStream.size(), 0, CommonErrorCodeEnum.FILE_READ_FAIL);
            //return ResponseEntity.notFound().build();
        }

        // 先设置 header 再写 responseStream，否则header会失效
        String fileName = URLEncoder.encode(businessType + "-import-template.csv", AppInfo.charset());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setCharacterEncoding(encoding);
        response.setContentLength(length);
        // 写入响应
        byteArrayOutputStream.writeTo(response.getOutputStream());
    }

}
