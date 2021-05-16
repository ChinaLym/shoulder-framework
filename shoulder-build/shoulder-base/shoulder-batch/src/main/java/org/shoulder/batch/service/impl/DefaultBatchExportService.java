package org.shoulder.batch.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.batch.cache.BatchProgressCache;
import org.shoulder.batch.constant.BatchConstants;
import org.shoulder.batch.enums.BatchErrorCodeEnum;
import org.shoulder.batch.enums.BatchI18nEnum;
import org.shoulder.batch.enums.ProcessStatusEnum;
import org.shoulder.batch.model.*;
import org.shoulder.batch.repository.BatchRecordDetailPersistentService;
import org.shoulder.batch.repository.BatchRecordPersistentService;
import org.shoulder.batch.service.BatchAndExportService;
import org.shoulder.batch.service.ext.BatchTaskSliceHandler;
import org.shoulder.core.dto.response.PageResult;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.i18.Translator;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.ArrayUtils;
import org.shoulder.core.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 处理导出门面
 *
 * @author lym
 */
public class DefaultBatchExportService implements BatchAndExportService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 默认的最大同时执行任务数，cpu 的两倍
     */
    private static final int DEFAULT_MAX_CONCURRENT_PROCESSOR = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * 批处理线程池
     */
    @Autowired
    @Qualifier(BatchConstants.THREAD_NAME)
    private ThreadPoolExecutor batchThreadPool;

    @Autowired
    protected Translator translator;

    /**
     * 所有导出器实现
     */
    @Autowired
    private List<DataExporter> dataExporterList;

    /**
     * 批量处理记录
     */
    @Autowired
    private BatchRecordPersistentService batchRecordPersistentService;

    /**
     * 处理详情
     */
    @Autowired
    protected BatchRecordDetailPersistentService batchRecordDetailPersistentService;

    @Autowired
    protected BatchProgressCache batchProgressCache;

    /**
     * 当前的导出器
     */
    private ThreadLocal<DataExporter> currentDataExporter = new ThreadLocal<>();

    /**
     * 当前的导出配置
     */
    private ThreadLocal<ExportConfig> exportConfigLocal = new ThreadLocal<>();

    /**
     * 是否额外生成详情列（当且仅当导出批量处理结果时）
     */
    private ThreadLocal<Boolean> exportRecordLocal = ThreadLocal.withInitial(() -> Boolean.FALSE);

    // ****************************  导出  *******************************

    /**
     * 导出
     *
     * @param outputStream     输出流
     * @param templateId       导出数据模板标识
     * @param dataSupplierList 导出数据
     * @throws IOException IO
     */
    @Override
    public void export(OutputStream outputStream, String exportType,
                       List<Supplier<List<Map<String, String>>>> dataSupplierList,
                       String templateId) throws IOException {

        // 初始化线程变量
        DataExporter dataExporter = dataExporterList.stream()
            .filter(exporter -> exporter.support(exportType))
            .findFirst().orElseThrow(() -> BatchErrorCodeEnum.EXPORT_TYPE_NOT_SUPPORT.toException(exportType));
        currentDataExporter.set(dataExporter);

        log.debug("find exporter {}", dataExporter);

        ExportConfig exportConfig = ExportSupport.getConfigWithLocale(templateId);
        if (exportConfig == null) {
            // 编码问题，未提供配置，需先调用 ExportSupport.putConfig 方法设置输出配置
            throw new BaseRuntimeException("templateId:" + templateId + " not existed! " +
                "Must invoke ExportSupport.putConfig before export");
        }
        exportConfigLocal.set(exportConfig);
        try {
            // 准备输出
            dataExporter.prepare(outputStream, exportConfig);
            // 输出头部信息
            outputHeader();
            log.trace("output headers finished.");
            // 输出数据
            log.debug("output data total turn: {}", dataSupplierList.size());
            for (int i = 0; i < dataSupplierList.size(); i++) {
                Supplier<List<Map<String, String>>> dataSupplier = dataSupplierList.get(i);
                List<Map<String, String>> exportDataList = dataSupplier.get();
                log.trace("output data turn {}", i);
                if (CollectionUtils.isNotEmpty(exportDataList)) {
                    outputData(exportDataList);
                }
            }
            log.trace("output data finished.");
            // 刷入流
            dataExporter.flush();
            // todo 【流程】记录业务日志
        } finally {
            // 清理上下文
            cleanContext();
        }
    }


    private void outputHeader() throws IOException {
        // 生成表头
        ExportConfig exportConfig = exportConfigLocal.get();
        if (CollectionUtils.isEmpty(exportConfig.getHeaders()) || CollectionUtils.isEmpty(exportConfig.getColumns())) {
            throw new BaseRuntimeException("descriptionList and columns can't be empty! ");
        }
        List<String[]> heads = exportConfig.getHeaders().stream()
            .map(ArrayUtils::toArray)
            .collect(Collectors.toList());
        List<ExportConfig.Column> columns = exportConfig.getColumns();
        String[] columnsName = new String[columns.size() + 3];
        List<String> nameList = columns.stream()
            .map(ExportConfig.Column::getColumnName)
            .collect(Collectors.toList());
        if (exportRecordLocal.get()) {
            nameList.add(BatchI18nEnum.ROW_NUM.i18nValue());
            nameList.add(BatchI18nEnum.RESULT.i18nValue());
            nameList.add(BatchI18nEnum.DETAIL.i18nValue());
        }
        heads.add(nameList.toArray(columnsName));

        currentDataExporter.get()
            .outputHeader(heads);
    }

    /**
     * 导出数据，不关闭流
     *
     * @param data         要导出的数据
     * @throws IOException IO异常
     */
    private void outputData(List<Map<String, String>> data) throws IOException {
        // 格式转换
        List<String[]> dataLine = data.stream()
            .map(this::toDataArray)
            .collect(Collectors.toList());
        currentDataExporter.get().outputData(dataLine);
    }

    /**
     * 将 Map 数据转为行
     *
     * @param dataMap 数据
     * @return 数据行
     */
    private String[] toDataArray(Map<String, String> dataMap) {
        ExportConfig exportConfig = exportConfigLocal.get();
        List<ExportConfig.Column> columnList = exportConfig.getColumns();
        String[] dataArray = new String[dataMap.size()];
        for (int i = 0; i < columnList.size(); i++) {
            ExportConfig.Column column = columnList.get(i);
            dataArray[i] = dataMap.get(column.getModelName());
        }
        return dataArray;
    }


    private void cleanContext() {
        currentDataExporter.get().cleanContext();
        currentDataExporter.remove();
        exportConfigLocal.remove();
        exportRecordLocal.remove();
    }


    @Override
    public void exportBatchDetail(OutputStream outputStream, String exportType, String templateId,
                                  String taskId, List<ProcessStatusEnum> resultTypes) throws IOException {
        exportRecordLocal.set(Boolean.TRUE);
        //认为单次批量操作一般有上限，如1000，这里直接单次全捞出来了
        export(outputStream, exportType, List.of(() -> {
            List<BatchRecordDetail> recordDetailList = findRecordDetailsByResults(taskId, resultTypes);
            return recordDetailList.stream()
                    .map(batchRecordDetail -> {
                        @SuppressWarnings("unchecked")
                        Map<String, String> dataMap = JsonUtils.parseObject(
                                batchRecordDetail.getSource(), Map.class, String.class, String.class);

                        dataMap.put(BatchConstants.INDEX, BatchI18nEnum.SPECIAL_ROW.i18nValue(batchRecordDetail.getIndex()));
                        dataMap.put(BatchConstants.RESULT, translator.getMessage(batchRecordDetail.getFailReason(),
                                ProcessStatusEnum.of(batchRecordDetail.getStatus()).getTip()));
                        dataMap.put(BatchConstants.DETAIL, translator.getMessage(batchRecordDetail.getFailReason()));
                    return dataMap;
                })
                .collect(Collectors.toList());
        }), templateId);
    }


    // *********************************  执行处理  **************************************

    /**
     * 判断是否允许处理
     *
     * @return boolean
     */
    @Override
    public boolean canExecute() {
        return batchThreadPool.getQueue().size() + batchThreadPool.getActiveCount() < DEFAULT_MAX_CONCURRENT_PROCESSOR;
    }

    /**
     * 进行批处理
     *
     * @param batchData             批处理数据入参
     * @param userId                用户信息
     * @param locale                语言标识
     * @param batchTaskSliceHandler 特殊业务处理器
     * @return 任务标识
     */
    @Override
    public String doProcess(BatchData batchData, String userId, Locale locale,
                            BatchTaskSliceHandler batchTaskSliceHandler) {
        if (!canExecute()) {
            throw BatchErrorCodeEnum.IMPORT_BUSY.toException();
        } else {
            BatchManager batchManager = new BatchManager(batchData);
            //执行持久化
            batchProgressCache.triggerFlushProgress(batchManager);
            batchThreadPool.execute(batchManager);
            return batchManager.getBatchProgress().getTaskId();
        }
    }

    // ------------------------  批量处理记录  --------------------

    /**
     * 获取处理进度与结果
     *
     * @param taskId 用户信息
     * @return 处理进度或者结果
     */
    @Override
    public BatchProgress queryBatchProgress(String taskId) {
        BatchProgress result = batchProgressCache.getTaskProgress(taskId);
        if (result == null) {
            // 缓存过期无需从数据库中查，直接异常
            throw BatchErrorCodeEnum.TASK_ID_NOT_EXIST.toException(taskId);
        }
        return result;
    }

    @Override
    public PageResult<BatchRecord> pageQueryRecord(String dataType, Integer pageNum, Integer pageSize,
                                                   String currentUserCode) {
        List<BatchRecord> dataList = batchRecordPersistentService.findByPage(dataType, pageNum, pageSize,
            currentUserCode);
        return PageResult.builder()
            .pageNum(pageNum)
            .pageSize(pageSize)
            .list(dataList)
            .build();

    }

    @Override
    public BatchRecord findLastRecord(String dataType, String currentUserName) {
        return batchRecordPersistentService.findLast(dataType, currentUserName);
    }

    /**
     * 构建查询参数
     */
    private Map<String, Object> generateCsvRecordMap(String tableName) {
        Map<String, Object> condition = new HashMap<>(2);
        condition.put("importType", tableName);
        return condition;
    }

    /**
     * 根据任务标识获取批量处理记录详情，用于处理完毕查看结果以及将结果导出
     *
     * @param importCode 任务标识
     * @return ImportRecord
     */
    @Override
    public BatchRecord findRecordById(String importCode) {
        return batchRecordPersistentService.findById(importCode);
    }


    // ------------------- 记录详情 ------------------------------

    @Override
    public List<BatchRecordDetail> findAllRecordDetail(String taskId) {
        return batchRecordDetailPersistentService.findAllByResult(taskId);
    }

    @Override
    public List<BatchRecordDetail> findRecordDetailsByResults(String taskId, List<ProcessStatusEnum> results) {
        if (CollectionUtils.isEmpty(results)) {
            return findAllRecordDetail(taskId);
        }
        return batchRecordDetailPersistentService.findAllByResult(taskId, results);
    }

}
