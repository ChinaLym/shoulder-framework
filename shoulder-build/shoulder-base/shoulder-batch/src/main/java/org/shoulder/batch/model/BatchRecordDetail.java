package org.shoulder.batch.model;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 批量执行记录详情-标准模型
 *
 * @author lym
 */
@Data
@Builder
@Accessors(chain = true)
public class BatchRecordDetail implements Serializable {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 批量执行记录表id
     */
    private String recordId;

    /**
     * 导入行号
     */
    private int rowNum;

    /**
     * 结果 0 导入成功 1 校验失败、2 重复跳过、3 重复更新、4 导入失败
     */
    private int result;

    /***
     * 操作类型
     */
    private String operation;

    /**
     * 失败原因，推荐支持多语言
     */
    private String failReason;

    /**
     * 导入的原数据
     */
    private String source;

    /**
     * 是否计算进度
     */
    private boolean calculateProgress = true;

    public BatchRecordDetail() {
    }

    public BatchRecordDetail(int rowNum, int result) {
        this.rowNum = rowNum;
        this.result = result;
    }

    public BatchRecordDetail(int rowNum, int result, String failReason) {
        this.rowNum = rowNum;
        this.result = result;
        this.failReason = failReason;
    }
}
