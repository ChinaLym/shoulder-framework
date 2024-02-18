
package org.shoulder.batch.spi;

import org.shoulder.batch.model.BatchData;
import org.shoulder.batch.model.BatchDataSlice;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.log.operation.annotation.OperationLog.Operations;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 专门处理导入任务分批
 * <hr>
 * （导入任务的）DataItem 为空，里面无实际数据，需要子线程处理时查询补充，避免数据量太大影响内存和GC
 *
 * @author lym
 */
public class ImportTaskSplitHandler implements TaskSplitHandler {

    @Override public boolean support(BatchData batchData) {
        return batchData.getOperation().equals(Operations.IMPORT);
    }

    @Override public List<BatchDataSlice> splitTask(BatchData batchData) {
        List<? extends DataItem> importItems = batchData.getBatchListMap().get(batchData.getOperation());
        BatchImportDataItem batchImportDataItem = fetchBatchImportDataItem(importItems);

        int total = batchImportDataItem.getTotal();
        int batchSliceSize = batchImportDataItem.getBatchSliceSize();

        int sliceNum = total / batchSliceSize + 1;
        List<BatchDataSlice> splitResult = IntStream.range(0, sliceNum)
            .mapToObj(sequence -> new BatchDataSlice(batchData.getBatchId(), sequence, batchData.getDataType(),
                batchData.getOperation(), importItems))
            .collect(Collectors.toList());

        return splitResult;
    }

    /**
     * 获取第一个元素，且必须为 BatchImportDataItem.class
     * @param importItems list
     * @return 第一个元素
     */
    public static BatchImportDataItem fetchBatchImportDataItem(List<? extends DataItem> importItems) {
        AssertUtils.notEmpty(importItems, CommonErrorCodeEnum.CODING);

        // 获取总条数
        BatchImportDataItem batchImportDataItem = importItems.stream()
            .findFirst()
            .map(item -> (BatchImportDataItem) item)
            .orElseThrow(() -> new BaseRuntimeException(CommonErrorCodeEnum.CODING));
        return batchImportDataItem;
    }
}
