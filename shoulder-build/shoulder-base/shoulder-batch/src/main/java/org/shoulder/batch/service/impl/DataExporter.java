package org.shoulder.batch.service.impl;

import org.shoulder.batch.model.ExportConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 数据导出
 * 实现类可为 CSV、Excel、Json 等自定义实现
 *
 * @author lym
 */
public interface DataExporter {


    /**
     * 是否支持某个导出方式
     *
     * @param exportType 导出方式
     * @return 是否支持
     */
    default boolean support(String exportType) {
        return false;
    }

    /**
     * 输出数据前准备操作
     *
     * @param outputStream 输出流
     * @param exportConfig 输出配置
     * @throws IOException IO 异常
     */
    void prepare(OutputStream outputStream, ExportConfig exportConfig) throws IOException;


    /**
     * 输出 头信息
     *
     * @param headers      头信息
     * @throws IOException IO 异常
     */
    void outputHeader(List<String[]> headers) throws IOException;


    /**
     * 输出 数据行
     *
     * @param dataLine     数据行
     * @throws IOException IO 异常
     */
    void outputData(List<String[]> dataLine) throws IOException;


    /**
     * 刷入输出流
     *
     * @throws IOException io
     */
    void flush() throws IOException;

    /**
     * 清理上下文，结束本次输出
     */
    default void cleanContext() {
    }

}
