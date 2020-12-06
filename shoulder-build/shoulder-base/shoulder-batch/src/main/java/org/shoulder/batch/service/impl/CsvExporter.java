package org.shoulder.batch.service.impl;

import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import org.shoulder.batch.enums.BatchConstants;
import org.shoulder.batch.model.ExportConfig;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * 数据导出-CSV
 *
 * @author lym
 */
public class CsvExporter implements DataExporter {

    private final Logger log = LoggerFactory.getLogger(getClass());


    private ThreadLocal<CsvWriter> writeLocal = new ThreadLocal<>();


    @Override
    public boolean support(String exportType) {
        return BatchConstants.CSV.equalsIgnoreCase(exportType);
    }


    /**
     * 输出数据前准备操作
     *
     * @param outputStream 输出流
     * @param exportConfig 输出配置
     * @throws IOException IO 异常
     */
    @Override
    public void prepare(OutputStream outputStream, ExportConfig exportConfig) throws IOException {
        CsvFormat csvFormat = new CsvFormat();
        csvFormat.setDelimiter(exportConfig.getSeparator());
        csvFormat.setLineSeparator(exportConfig.getLineSeparator());
        csvFormat.setComment(exportConfig.getComment());
        csvFormat.setQuote(exportConfig.getQuote());
        csvFormat.setQuoteEscape(exportConfig.getQuoteEscape());
        CsvWriterSettings csvWriterSettings = new CsvWriterSettings();
        csvWriterSettings.setFormat(csvFormat);
        CsvWriter writer = new CsvWriter(
            new BufferedWriter(new OutputStreamWriter(outputStream, AppInfo.charset())), csvWriterSettings);
        log.trace("prepare for export");
        writeLocal.set(writer);
    }

    /**
     * 输出头
     *
     * @param headers      头信息
     * @throws IOException IO 异常
     */
    @Override
    public void outputHeader(List<String[]> headers) throws IOException {
        CsvWriter writer = writeLocal.get();
        headers.forEach(writer::writeRow);
    }

    /**
     * 输出
     *
     * @param dataLine     一行数据
     * @throws IOException IO 异常
     */
    @Override
    public void outputData(List<String[]> dataLine) throws IOException {
        CsvWriter writer = writeLocal.get();
        dataLine.forEach(writer::writeRow);
        // todo 【性能】 是否调用 flush
        writer.flush();
    }


    /**
     * 刷入流
     */
    @Override
    public void flush() {
        writeLocal.get().flush();
    }

    @Override
    public void cleanContext() {
        writeLocal.remove();
    }


}
