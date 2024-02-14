package org.shoulder.batch.service.csv;

import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import org.shoulder.batch.config.model.ExportFileConfig;
import org.shoulder.batch.constant.BatchConstants;
import org.shoulder.batch.service.impl.DataExporter;
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
     * @param outputStream     输出流
     * @param exportFileConfig 输出配置
     * @throws IOException IO 异常
     */
    @Override
    public void prepare(OutputStream outputStream, ExportFileConfig exportFileConfig) throws IOException {
        CsvFormat csvFormat = new CsvFormat();
        csvFormat.setDelimiter(exportFileConfig.getSeparator());
        csvFormat.setLineSeparator(exportFileConfig.getLineSeparator());
        csvFormat.setComment(exportFileConfig.getComment());
        csvFormat.setQuote(exportFileConfig.getQuote());
        csvFormat.setQuoteEscape(exportFileConfig.getQuoteEscape());
        CsvWriterSettings csvWriterSettings = new CsvWriterSettings();
        csvWriterSettings.setFormat(csvFormat);
        CsvWriter writer = new CsvWriter(
            new BufferedWriter(new OutputStreamWriter(outputStream, exportFileConfig.getEncode())), csvWriterSettings);
        log.trace("prepare for export");
        writeLocal.set(writer);
    }

    /**
     * 输出头
     *
     * @param headers 头信息
     * @throws IOException IO 异常
     */
    @Override
    public void outputHeader(List<String> headers) throws IOException {
        CsvWriter writer = writeLocal.get();
        // todo 输出header 第一列，会被 引号 包围：
        writer.writeHeaders(headers);
    }

    /**
     * 输出
     *
     * @param dataLine 一行数据
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
