
package org.shoulder.batch.spi.csv;

import com.univocity.parsers.common.record.IndexedRecordImpl;
import com.univocity.parsers.common.record.Record;
import org.shoulder.batch.spi.DataItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lym
 */
public class DefaultDataItemConvertFactory implements DataItemConvertFactory {

    @Override public List<? extends DataItem> convertRecordToDataItem(String dataType, List<Record> recordList) {
        List<IndexedRecordImpl> resultList = new ArrayList<>(recordList.size());
        // 跳过 header 行，只处理数据行
        for (int i = 1; i < recordList.size(); i++) {
            resultList.add(new IndexedRecordImpl(recordList.get(i), i));
        }
        return resultList;
    }
}
