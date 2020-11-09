package org.shoulder.batch.service;

import org.shoulder.batch.enums.ImportResultEnum;
import org.shoulder.core.exception.BaseRuntimeException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * 导出业务
 *
 * @author lym
 */
public interface ExportService {

    /**
     * 导出数据列表
     *
     * @param languageId    语言信息
     * @param outputStream  输出流
     * @param exportModelId 导出模型标识
     * @param queryDTO      查询条件
     * @param exportType    查询条件
     * @throws IOException io异常
     */
    void export(String languageId, OutputStream outputStream,
                String exportModelId, DataQueryDTO queryDTO, String exportType) throws IOException;

    /**
     * 导出 导入的详情列表
     *
     * @param languageId     语言信息
     * @param outputStream   输出流
     * @param exportModelId  导出模型标识
     * @param importResultId 导入结果标识
     * @param type           详情类型
     * @param exportType
     * @throws IOException io异常
     */
    void exportImportDetail(String languageId, OutputStream outputStream, String exportModelId,
                            String importResultId, List<ImportResultEnum> type, String exportType) throws IOException;


    /**
    * <p>给导出的文件命名<p>
    *
    * @param response http 响应
    * @param fileName 导出文件名
    */
    default void setExportFileName(HttpServletResponse response, String fileName) {
        try {
            response.setHeader("Content-Disposition", "attachment; filename=" +
                    URLEncoder.encode(fileName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new BaseRuntimeException(e);
        }
        response.setHeader("Content-Type", "application/octet-stream");
    }

}
