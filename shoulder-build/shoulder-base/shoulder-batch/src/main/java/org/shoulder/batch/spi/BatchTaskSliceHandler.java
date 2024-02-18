package org.shoulder.batch.spi;

import org.shoulder.batch.model.BatchDataSlice;
import org.shoulder.batch.model.BatchRecordDetail;

import java.util.List;

/**
 * 任务分片处理器
 * 使用者自行实现该接口，可在这里处理
 * <hr>
 * 举例：
 * <li> 处理导入前的校验</li>
 * <li>处理导入-保存</li>
 *
 * @author lym
 */
public interface BatchTaskSliceHandler {

    /**
     * 是否支持
     *
     * @param dataType      数据类型
     * @param operationType 操作方式
     * @return 是否支持
     */
    boolean support(String dataType, String operationType);

    /**
     * 处理数据
     *
     * @param batchSlice 任务，注意不要对该对象改动
     * @return 处理结果，注意长度必须等于 batchList.size 否则认为部分处理失败
     */
    List<BatchRecordDetail> handle(BatchDataSlice batchSlice);

}
