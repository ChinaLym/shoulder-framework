package org.shoulder.batch.spi.csv;

import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import org.shoulder.batch.config.model.ExportFileConfig;
import org.shoulder.batch.constant.BatchConstants;
import org.shoulder.batch.log.ShoulderBatchLoggers;
import org.shoulder.batch.service.BatchOutputContext;
import org.shoulder.batch.spi.DataExporter;
import org.shoulder.core.log.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.StringJoiner;

/**
 * 数据导出-CSV
 *
 * @author lym
 */
public class CsvExporter implements DataExporter {

    private final Logger log = ShoulderBatchLoggers.DEFAULT;
    private static final String LOCAL_CSV_WRITER = "CsvWriter";
    private static final String LOCAL_BUFFER_WRITER = "BufferedWriter";
    private static final String LOCAL_CSV_WRITER_SETTINGS = "CsvWriterSettings";

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
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream, exportFileConfig.getEncode()));
        CsvWriter writer = new CsvWriter(bw, csvWriterSettings);
        log.trace("prepare for export");
        BatchOutputContext.get().putExtValue(LOCAL_CSV_WRITER, writer);
        BatchOutputContext.get().putExtValue(LOCAL_BUFFER_WRITER, bw);
        BatchOutputContext.get().putExtValue(LOCAL_CSV_WRITER_SETTINGS, csvWriterSettings);
    }

    @Override
    public void outputComment(List<String> commentLines) throws IOException {
        BufferedWriter writer = BatchOutputContext.get().getExtValue(LOCAL_BUFFER_WRITER);
        CsvWriterSettings csvWriterSettings = BatchOutputContext.get().getExtValue(LOCAL_CSV_WRITER_SETTINGS);
        String split = csvWriterSettings.getFormat().getLineSeparatorString();
        String commentPrefix = "" + csvWriterSettings.getFormat().getComment();
        StringJoiner sj = new StringJoiner(split + commentPrefix, commentPrefix, "");
        commentLines.forEach(sj::add);
        writer.write(sj.toString());
    }

    /**
     * 输出头
     *
     * @param headers 头信息
     * @throws IOException IO 异常
     */
    @Override
    public void outputHeader(List<String> headers) throws IOException {
        CsvWriter writer = BatchOutputContext.get().getExtValue(LOCAL_CSV_WRITER);
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
        CsvWriter writer = BatchOutputContext.get().getExtValue(LOCAL_CSV_WRITER);
        dataLine.forEach(writer::writeRow);
        writer.flush();
    }

    /**
     * 刷入流
     */
    @Override
    public void flush() {
        CsvWriter writer = BatchOutputContext.get().getExtValue(LOCAL_CSV_WRITER);
        writer.flush();
    }

}
