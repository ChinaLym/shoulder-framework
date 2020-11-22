package org.shoulder.batch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.shoulder.batch.repository.po.BatchRecordDetailPO;

import java.io.Serializable;

/**
 * 批量处理记录详情-标准模型
 *
 * @author lym
 */
@Data
@Builder
@Accessors(chain = true)
@AllArgsConstructor
public class BatchRecordDetail implements Serializable {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 批量处理记录表id
     */
    private String recordId;

    /**
     * 本次批处理所在位置行号 / 索引 / 下标
     */
    private int rowNum;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 处理结果状态 0 处理成功 1 校验失败、2 重复跳过、3 重复更新、4 处理失败
     */
    private int status;

    /**
     * 失败原因，推荐支持多语言
     */
    private String failReason;

    /**
     * 处理的原始数据
     */
    private String source;

    /**
     * 是否计算进度
     */
    private boolean calculateProgress = true;

    public BatchRecordDetail() {
    }

    public BatchRecordDetail(int rowNum, int status) {
        this.rowNum = rowNum;
        this.status = status;
    }

    public BatchRecordDetail(int rowNum, int status, String failReason) {
        this.rowNum = rowNum;
        this.status = status;
        this.failReason = failReason;
    }


    public BatchRecordDetail(BatchRecordDetailPO po) {
        id = po.getId();
        recordId = po.getRecordId();
        rowNum = po.getRowNum();
        operation = po.getOperation();
        status = po.getStatus();
        failReason = po.getFailReason();
        source = po.getSource();
    }

    public BatchRecordDetailPO toPersistent() {
        return BatchRecordDetailPO.builder()
            .id(id)
            .recordId(recordId)
            .rowNum(rowNum)
            .operation(operation)
            .operation(operation)
            .status(status)
            .failReason(failReason)
            .source(source)
            .build();
    }

}
