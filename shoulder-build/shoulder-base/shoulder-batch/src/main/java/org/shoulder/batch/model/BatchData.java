package org.shoulder.batch.model;

import lombok.Data;
import org.shoulder.batch.service.impl.BatchManager;
import org.shoulder.batch.spi.DataItem;

import java.util.List;

/**
 * 批量任务
 * 校验完毕后，组装成该模型，传入Service，批处理管理类（{@link BatchManager}）会将本类分割转化为 {@link BatchDataSlice}
 *
 * @author lym
 */
@Data
public class BatchData {

    /**
     * 批处理 id
     */
    protected String batchId;

    /**
     * 业务标识 / 数据类型
     */
    protected String dataType;

    /**
     * 操作类型
     */
    protected String operation;

    /**
     * 需要批处理的数据
     */
    protected List<? extends DataItem> dataList;

    /**
     * 保存这次操作记录
     */
    protected boolean persistentRecord = true;

    public BatchData() {
    }

}
