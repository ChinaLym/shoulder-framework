
package org.shoulder.batch.spi.csv;

import com.univocity.parsers.common.record.Record;
import org.shoulder.batch.spi.DataItem;

import java.util.List;

/**
 * 将 csv 导入记录转为 batch 模块模型
 *
 * @author lym
 */
public interface DataItemConvertFactory {

    List<? extends DataItem> convertRecordToDataItem(String dataType, List<Record> recordList);

}
