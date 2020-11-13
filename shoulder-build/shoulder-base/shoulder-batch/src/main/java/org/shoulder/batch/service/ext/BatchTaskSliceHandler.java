package org.shoulder.batch.service.ext;

import org.shoulder.batch.model.BatchDataSlice;
import org.shoulder.batch.model.BatchRecordDetail;

import java.util.List;

/**
 * 任务分片处理器
 * 使用者自行实现该接口，可在这里处理
 *
 * @author lym
 */
public interface BatchTaskSliceHandler {

    /**
     * 处理数据
     *
     * @param task 任务，注意不要对该对象改动
     * @return 处理结果，注意长度必须等于 batchList.size 否则认为部分处理失败
     */
    List<BatchRecordDetail> handle(BatchDataSlice task);


    /*List<? extends DataItem> dataList = task.getBatchList();
    List<ImportRecordDetail> resultList = new LinkedList<>();
        for (DataItem dataItem : dataList) {
        ImportRecordDetail result = new ImportRecordDetail();
        result.setRowNum(dataItem.getRowNum());
        // doing process 如保存 db 等
        result.setResult(BatchResultEnum.IMPORT_SUCCESS.getCode());

        resultList.add(result);
    }
        return resultList;*/

    /**
     * 是否支持
     *
     * @param dataType      数据类型
     * @param operationType 操作方式
     * @return 是否支持
     */
    default boolean support(String dataType, String operationType) {
        return false;
    }

}
