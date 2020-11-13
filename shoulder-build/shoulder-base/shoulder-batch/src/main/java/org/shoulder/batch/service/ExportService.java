package org.shoulder.batch.service;

import org.shoulder.batch.enums.BatchResultEnum;
import org.shoulder.core.context.AppInfo;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
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
     * @param outputStream  输出流
     * @param exportType    导出方式：CSV / EXCEL
     * @param exportData    要导出的数据（支持分片，渐进式导出，避免内存过大）
     * @param exportModelId 导出数据标识
     * @throws IOException io异常
     */
    void export(OutputStream outputStream, String exportType, List<Supplier<List<Map<String, String>>>> exportData,
                String exportModelId)
        throws IOException;

    /**
     * 导出 导入的详情列表
     *
     * @param outputStream  输出流
     * @param exportType    导出方式，如 CSV、EXCEL
     * @param exportModelId 导出数据标识
     * @param taskId        记录标识
     * @param resultTypes   执行结果
     * @throws IOException io异常
     */
    void exportBatchDetail(OutputStream outputStream, String exportType, String exportModelId,
                           String taskId, List<BatchResultEnum> resultTypes) throws IOException;


    /**
     * 给导出的文件命名
     *
     * @param response http 响应
     * @param fileName 导出文件名
     * @deprecated 不要在这里做
     */
    default void setExportFileName(HttpServletResponse response, String fileName) {
        response.setHeader("Content-Disposition", "attachment; filename=" +
            URLEncoder.encode(fileName, AppInfo.charset()));
        response.setHeader("Content-Type", "application/octet-stream");
    }

}
