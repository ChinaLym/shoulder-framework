
package org.shoulder.batch.spi;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.shoulder.batch.model.BatchData;
import org.shoulder.batch.model.BatchDataSlice;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 默认任务分批
 * <hr>
 * 每 200 条数据分为一组
 *
 * @author lym
 */
public class DefaultTaskSplitHandler implements TaskSplitHandler {

    /**
     * 添加数据默认单次处理最大数目
     */
    private final int taskSliceMax;

    public DefaultTaskSplitHandler(int taskSliceMax) {
        //  todo 【增强】可配置，可按照操作配置
        this.taskSliceMax = taskSliceMax;
    }

    @Override
    public boolean support(BatchData batchData) {
        return true;
    }

    @Override
    public List<BatchDataSlice> splitTask(BatchData batchData) {
        // 默认将每类任务划分为一片、每片最多200个
        if (CollectionUtils.isEmpty(batchData.getDataList())) {
            return Collections.emptyList();
        }
        List<BatchDataSlice> tasks = new LinkedList<>();
        AtomicInteger sequence = new AtomicInteger(0);
        // 切片
        List<? extends List<? extends DataItem>> pages = ListUtils.partition(batchData.getDataList(), taskSliceMax);
        for (List<? extends DataItem> page : pages) {
            if (CollectionUtils.isNotEmpty(page)) {
                tasks.add(new BatchDataSlice(batchData.getBatchId(), sequence.getAndIncrement(),
                    batchData.getDataType(), batchData.getOperation(), page)
                );
            }
        }
        return tasks;
    }

}
