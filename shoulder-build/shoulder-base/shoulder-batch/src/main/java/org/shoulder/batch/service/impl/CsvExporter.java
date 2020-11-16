package org.shoulder.batch.service.impl;

import com.opencsv.CSVWriter;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import org.shoulder.batch.enums.ExportConstants;
import org.shoulder.core.context.AppInfo;
import org.springframework.stereotype.Service;

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
@Service
public class CsvExporter implements DataExporter {

    // todo 可配置？ 跟随语言标识，使用地域默认
    private char separator = CSVWriter.DEFAULT_SEPARATOR;
    private char quote = CSVWriter.DEFAULT_QUOTE_CHARACTER;
    private char escape = CSVWriter.DEFAULT_ESCAPE_CHARACTER;
    private String lineEnd = CSVWriter.DEFAULT_LINE_END;

    private ThreadLocal<CsvWriter> writeLocal = new ThreadLocal<>();


    @Override
    public boolean support(String exportType) {
        return ExportConstants.CSV.equalsIgnoreCase(exportType);
    }


    /**
     * 输出头
     *
     * @param outputStream 输出流
     * @param headers      头信息
     * @throws IOException IO 异常
     */
    @Override
    public void outputHeader(OutputStream outputStream, List<String[]> headers) throws IOException {
        /*CSVWriter writer = new CSVWriter(
            new BufferedWriter(new OutputStreamWriter(outputStream, AppInfo.charset())),
            separator, quote, escape, lineEnd);*/
        // todo 为每种语言单独处理
        CsvWriterSettings csvWriterSettings = new CsvWriterSettings();
        CsvWriter writer = new CsvWriter(
            new BufferedWriter(new OutputStreamWriter(outputStream, AppInfo.charset())), csvWriterSettings);
        writeLocal.set(writer);
        headers.forEach(writer::writeRow);
    }

    /**
     * 输出
     *
     * @param outputStream 输出流
     * @param dataLine     一行数据
     * @throws IOException IO 异常
     */
    @Override
    public void outputData(OutputStream outputStream, List<String[]> dataLine) throws IOException {

        dataLine.forEach(writeLocal.get()::writeRow);
        // todo flush ?
        writeLocal.get().flush();
    }


    /**
     * 刷入流
     *
     * @param outputStream 输出流
     */
    @Override
    public void flush(OutputStream outputStream) {
        writeLocal.get().flush();
    }

    @Override
    public void cleanContext() {
        writeLocal.remove();
    }


}
