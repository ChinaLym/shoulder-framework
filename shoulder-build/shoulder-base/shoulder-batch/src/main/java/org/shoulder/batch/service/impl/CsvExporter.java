package org.shoulder.batch.service.impl;

import cn.hutool.core.text.csv.CsvWriter;
import com.opencsv.CSVWriter;
import org.shoulder.batch.enums.ExportConstants;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.i18.Translator;
import org.springframework.beans.factory.annotation.Autowired;
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

    // todo 可配置？
    private char separator = CSVWriter.DEFAULT_SEPARATOR;
    private char quote = CSVWriter.DEFAULT_QUOTE_CHARACTER;
    private char escape = CSVWriter.DEFAULT_ESCAPE_CHARACTER;
    private String lineEnd = CSVWriter.DEFAULT_LINE_END;

    @Autowired
    protected Translator translator;

    @Autowired
    private CsvWriter csvWriter;


    @Override
    public boolean support(String exportType) {
        return ExportConstants.CSV.equalsIgnoreCase(exportType);
    }


    /**
     * 输出
     *
     * @param outputStream 输出流
     * @param dataLine     一行数据
     * @throws IOException IO 异常
     */
    @Override
    public void outputDataArray(OutputStream outputStream, List<String[]> dataLine) throws IOException {
        CSVWriter writer = new CSVWriter(
            new BufferedWriter(new OutputStreamWriter(outputStream, AppInfo.charset())),
            separator, quote, escape, lineEnd);
        writer.writeAll(dataLine);
        // todo flush ?
        writer.flush();
    }


    /**
     * 刷入流
     *
     * @param outputStream 输出流
     */
    @Override
    public void flush(OutputStream outputStream) {
        csvWriter.flush();
    }


}
