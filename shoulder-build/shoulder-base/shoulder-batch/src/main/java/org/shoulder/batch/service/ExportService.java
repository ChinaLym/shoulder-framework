package org.shoulder.batch.service;

import org.shoulder.batch.enums.ProcessStatusEnum;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 导出业务
 *
 * @author lym
 */
public interface ExportService {

    /**
     * 导出数据列表
     *
     * @param outputStream 输出流
     * @param exportType   导出方式：CSV / EXCEL
     * @param exportData   要导出的数据（支持分片，渐进式导出，避免内存过大）
     * @param templateId   导出模板标识，如 user_batch_add、user_batch_update
     * @throws IOException io异常
     */
    void export(OutputStream outputStream, String exportType, List<Supplier<List<Map<String, String>>>> exportData,
                String templateId)
        throws IOException;

    /**
     * 导出 批处理详情列表
     *
     * @param outputStream 输出流
     * @param exportType   导出方式，如 CSV、EXCEL
     * @param templateId   导出数据标识
     * @param batchId      记录标识
     * @param resultTypes  执行结果
     * @throws IOException io异常
     */
    void exportBatchDetail(OutputStream outputStream, String exportType, String templateId,
                           String batchId, List<ProcessStatusEnum> resultTypes) throws IOException;

}
