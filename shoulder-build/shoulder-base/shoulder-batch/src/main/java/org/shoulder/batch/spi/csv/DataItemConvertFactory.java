/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package org.shoulder.batch.spi.csv;

import com.univocity.parsers.common.record.Record;
import org.shoulder.batch.spi.DataItem;

import java.util.List;

/**
 * @author lym
 */
public interface DataItemConvertFactory {

    List<? extends DataItem> convertRecordToDataItem(String dataType, List<Record> recordList);

}
