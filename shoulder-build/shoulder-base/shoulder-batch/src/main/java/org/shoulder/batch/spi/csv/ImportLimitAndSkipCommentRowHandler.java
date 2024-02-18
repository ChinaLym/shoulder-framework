
package org.shoulder.batch.spi.csv;

import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvRowHandler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * csv导入时，限制导入总条数，并跳过注释行，作为导入接口的同步校验器
 * @deprecated 暂未使用
 *
 * @author lym
 */
public class ImportLimitAndSkipCommentRowHandler implements CsvRowHandler {

    private final AtomicInteger maxRowNum;

    public ImportLimitAndSkipCommentRowHandler(int max) {
        this.maxRowNum = new AtomicInteger(max);
    }

    //private final Class<?> dataCsvImportClass;

    //private final ConversionService conversionService;

    /*
    public ImportLimitAndSkipCommentRowHandler(int max, Class<?> dataCsvImportClass, ConversionService conversionService) {
        this.maxRowNum = new AtomicInteger(max);
        this.dataCsvImportClass = dataCsvImportClass;
        this.conversionService = conversionService;
    }
    */

    @Override
    public void handle(CsvRow csvRow) {
        //Object csvRealObj = conversionService.convert(csvRow, dataCsvImportClass);
        //ValidateUtil.validate(csvRealObj);
    }
}
