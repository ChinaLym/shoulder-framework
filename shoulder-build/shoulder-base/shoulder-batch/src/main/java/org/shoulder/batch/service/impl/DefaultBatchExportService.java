package org.shoulder.batch.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.batch.config.ExportConfigManager;
import org.shoulder.batch.config.model.ExportColumnConfig;
import org.shoulder.batch.config.model.ExportFileConfig;
import org.shoulder.batch.constant.BatchConstants;
import org.shoulder.batch.enums.BatchDetailResultStatusEnum;
import org.shoulder.batch.enums.BatchErrorCodeEnum;
import org.shoulder.batch.enums.BatchI18nEnum;
import org.shoulder.batch.model.BatchData;
import org.shoulder.batch.model.BatchRecord;
import org.shoulder.batch.model.BatchRecordDetail;
import org.shoulder.batch.progress.BatchProgressCache;
import org.shoulder.batch.progress.BatchProgressRecord;
import org.shoulder.batch.progress.ProgressAble;
import org.shoulder.batch.repository.BatchRecordDetailPersistentService;
import org.shoulder.batch.repository.BatchRecordPersistentService;
import org.shoulder.batch.service.BatchAndExportService;
import org.shoulder.batch.spi.BatchTaskSliceHandler;
import org.shoulder.batch.spi.DataExporter;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.dto.response.PageResult;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.i18.Translator;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
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

    protected final ThreadPoolExecutor batchThreadPool;

    protected final Translator translator;

    /**
     * 所有导出器实现
     */

    protected final List<DataExporter> dataExporterList;

    /**
     * 批量处理记录
     */

    protected final BatchRecordPersistentService batchRecordPersistentService;

    /**
     * 处理详情
     */

    protected final BatchRecordDetailPersistentService batchRecordDetailPersistentService;

    protected final BatchProgressCache batchProgressCache;

    protected final ExportConfigManager exportConfigManager;

    // ---------------------------------------

    /**
     * 当前的导出器
     */
    private ThreadLocal<DataExporter> currentDataExporter = new ThreadLocal<>();

    /**
     * 当前的导出配置
     */
    private ThreadLocal<ExportFileConfig> exportConfigLocal = new ThreadLocal<>();

    /**
     * 是否额外生成详情列（当且仅当导出批量处理结果时）
     */
    private ThreadLocal<Boolean> exportRecordLocal = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public DefaultBatchExportService(ThreadPoolExecutor batchThreadPool, Translator translator, List<DataExporter> dataExporterList,
                                     BatchRecordPersistentService batchRecordPersistentService,
                                     BatchRecordDetailPersistentService batchRecordDetailPersistentService,
                                     BatchProgressCache batchProgressCache, ExportConfigManager exportConfigManager) {
        this.batchThreadPool = batchThreadPool;
        this.translator = translator;
        this.dataExporterList = dataExporterList;
        this.batchRecordPersistentService = batchRecordPersistentService;
        this.batchRecordDetailPersistentService = batchRecordDetailPersistentService;
        this.batchProgressCache = batchProgressCache;
        this.exportConfigManager = exportConfigManager;
    }

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
    public String export(OutputStream outputStream, String exportType,
                         List<Supplier<List<Map<String, String>>>> dataSupplierList,
                         String templateId) throws IOException {
        // 初始化线程变量
        DataExporter dataExporter = dataExporterList.stream()
            .filter(exporter -> exporter.support(exportType))
            .findFirst().orElseThrow(() -> BatchErrorCodeEnum.EXPORT_TYPE_NOT_SUPPORT.toException(exportType));
        currentDataExporter.set(dataExporter);

        log.debug("find exporter {}", dataExporter);

        ExportFileConfig exportFileConfig = exportConfigManager.getFileConfigWithLocale(templateId, AppContext.getLocaleOrDefault());
        if (exportFileConfig == null) {
            // 编码问题，未提供配置，需先调用 ExportConfigManager.putConfig 方法设置输出配置
            throw new BaseRuntimeException("templateId:" + templateId + " not existed! ");
        }
        exportConfigLocal.set(exportFileConfig);
        try {
            // 准备输出
            dataExporter.prepare(outputStream, exportFileConfig);
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
            return exportFileConfig.getEncode();
            // todo 【流程】记录业务日志
        } finally {
            // 清理上下文
            cleanContext();
        }
    }

    /**
     * 生成完整表头（介绍 + 字段 + demo）
     */
    private void outputHeader() throws IOException {
        ExportFileConfig exportFileConfig = exportConfigLocal.get();
        boolean exportRecordInfo = exportRecordLocal.get();
        if (CollectionUtils.isEmpty(exportFileConfig.getHeaders()) || CollectionUtils.isEmpty(exportFileConfig.getColumns())) {
            throw new BaseRuntimeException("descriptionList and columns can't be empty! ");
        }

        List<ExportColumnConfig> columns = exportFileConfig.getColumns();
        List<String> nameList = columns.stream()
            .map(ExportColumnConfig::getColumnName)
            .collect(Collectors.toList());
        if (exportRecordInfo) {
            nameList.add(BatchI18nEnum.ROW_NUM.i18nValue());
            nameList.add(BatchI18nEnum.RESULT.i18nValue());
            nameList.add(BatchI18nEnum.DETAIL.i18nValue());
        }
        String[] columnsName = new String[columns.size() + (exportRecordInfo ? 3 : 0)];

        currentDataExporter.get()
            .outputHeader(exportFileConfig.getHeaders());
        currentDataExporter.get()
            .outputData(Collections.singletonList(nameList.toArray(columnsName)));
    }

    /**
     * 导出数据，不关闭流
     *
     * @param data 要导出的数据
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
        ExportFileConfig exportFileConfig = exportConfigLocal.get();
        List<ExportColumnConfig> columnList = exportFileConfig.getColumns();
        String[] dataArray = new String[dataMap.size()];
        for (int i = 0; i < columnList.size(); i++) {
            ExportColumnConfig column = columnList.get(i);
            dataArray[i] = dataMap.get(column.getModelFieldName());
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
    public String exportBatchDetail(OutputStream outputStream, String exportType, String templateId,
                                    String batchId, List<BatchDetailResultStatusEnum> resultTypes) throws IOException {
        exportRecordLocal.set(Boolean.TRUE);
        // 认为单次批量操作一般有上限，如1000，这里直接单次全捞出来了
        return export(outputStream, exportType, List.of(() -> {
            List<BatchRecordDetail> recordDetailList = findAllDetailByRecordIdAndStatusAndIndex(batchId, resultTypes, null, null);
            return recordDetailList.stream()
                .map(batchRecordDetail -> {
                    @SuppressWarnings("unchecked")
                    Map<String, String> dataMap = JsonUtils.parseObject(
                        batchRecordDetail.getSource(), Map.class, String.class, String.class);

                    dataMap.put(BatchConstants.INDEX, BatchI18nEnum.SPECIAL_ROW.i18nValue(batchRecordDetail.getIndex()));
                    dataMap.put(BatchConstants.RESULT, translator.getMessage(
                            BatchDetailResultStatusEnum.of(batchRecordDetail.getStatus()).getTip()));
                    dataMap.put(BatchConstants.DETAIL, StringUtils.isBlank(batchRecordDetail.getFailReason()) ? null :
                        translator.getMessage(batchRecordDetail.getFailReason()));
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
     * @return 批处理任务id
     */
    @Override
    public String doProcess(BatchData batchData, String userId, Locale locale,
                            BatchTaskSliceHandler batchTaskSliceHandler) {
        if (!canExecute()) {
            throw BatchErrorCodeEnum.IMPORT_BUSY.toException();
        } else {
            BatchManager batchManager = new BatchManager(batchData);
            //执行持久化
            batchProgressCache.triggerFlushProgress(batchManager.getBatchProgress());
            batchThreadPool.execute(batchManager);
            return batchManager.getBatchProgress().getId();
        }
    }

    // ------------------------  批量处理记录  --------------------

    /**
     * 获取处理进度与结果
     *
     * @param batchId 用户信息
     * @return 处理进度或者结果
     */
    @Override
    public BatchProgressRecord queryBatchProgress(String batchId) {
        ProgressAble result = batchProgressCache.getProgress(batchId);
        if (result == null) {
            // 缓存过期无需从数据库中查，直接异常
            throw BatchErrorCodeEnum.BATCH_ID_NOT_EXIST.toException(batchId);
        }
        return result.getBatchProgress();
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
     * 根据批处理任务id获取批量处理记录详情，用于处理完毕查看结果以及将结果导出
     *
     * @param importCode 批处理任务id
     * @return ImportRecord
     */
    @Override
    public BatchRecord findRecordById(String importCode) {
        return batchRecordPersistentService.findById(importCode);
    }

    // ------------------- 记录详情 ------------------------------

    @Override
    public List<BatchRecordDetail> findAllDetailByRecordIdAndStatusAndIndex(String recordId, List<BatchDetailResultStatusEnum> resultList,
                                                                            Integer indexStart, Integer indexEnd) {
        return batchRecordDetailPersistentService.findAllByRecordIdAndStatusAndIndex(recordId, resultList, indexStart, indexEnd);
    }

}
