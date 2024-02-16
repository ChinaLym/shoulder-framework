package org.shoulder.batch.spi;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 专门处理导入任务分批，导入时 DataItem 无真正数据，仅用于标识总数，子任务来捞取数据
 *
 * @author lym
 */
@Getter public class BatchImportDataItem implements DataItem {

    public final static String EXT_KEY_UPDATE_REPEAT = "updateRepeat";

    /**
     * 总量
     */
    private final int total;

    /**
     * 单线程处理的数据量
     */
    private final int batchSliceSize;

    /**
     * 源任务id
     */
    private final String sourceBatchId;

    /**
     * 扩展属性
     */
    private final Map<String, Object> extAttributeMap = new HashMap<>();

    public BatchImportDataItem(int total, int batchSliceSize, String sourceBatchId, Map<String, Object> extAttributeMap) {
        this.total = total;
        this.batchSliceSize = batchSliceSize;
        this.sourceBatchId = sourceBatchId;
        this.extAttributeMap.putAll(extAttributeMap);
    }

    @Override public int getIndex() {
        return 0;
    }

    public <T> T getExtAttribute(String attributeKey) {
        return (T) extAttributeMap.get(attributeKey);
    }

    public <T> T setExtAttribute(String attributeKey, T value) {
        return (T) extAttributeMap.put(attributeKey, value);
    }
}
