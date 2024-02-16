package org.shoulder.batch.spi;

import lombok.Getter;

/**
 * 专门处理导入任务分批，导入时 DataItem 无真正数据，仅用于标识总数，子任务来捞取数据
 *
 * @author lym
 */
@Getter public class BatchImportDataItem implements DataItem {

    private final int total;


    public BatchImportDataItem(int total) {this.total = total;}

    @Override public int getIndex() {
        return 0;
    }
}
