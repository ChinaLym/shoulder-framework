/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
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
        for (int i = 0; i < recordList.size(); i++) {
            resultList.add(new IndexedRecordImpl(recordList.get(i), i));
        }
        return resultList;
    }
}
