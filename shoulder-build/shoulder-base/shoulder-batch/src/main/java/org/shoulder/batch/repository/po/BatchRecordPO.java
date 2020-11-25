package org.shoulder.batch.repository.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.shoulder.batch.model.BatchRecord;

import java.io.Serializable;
import java.util.Date;

/**
 * 批量处理记录-标准模型
 *
 * @author lym
 */
@Data
@Builder
@AllArgsConstructor
public class BatchRecordPO implements Serializable {

    /**
     * 主键
     */
    private String id;

    /**
     * 操作数据类型，建议可翻译。如对应 导入数据库表名、业务线名，例：用户、用户组、推广消息
     */
    private String dataType;

    /**
     * 操作类型，建议可翻译。如对应 动作名称，例：导入、同步、推送、下载
     */
    private String operation;

    // 目前 不区分操作类型，而是根据 detail 的结果分辨 operationType;

    /**
     * 处理总数
     */
    private int totalNum;

    /**
     * 成功条数
     */
    private int successNum;

    /**
     * 失败条数
     */
    private int failNum;

    /**
     * 触发处理的用户
     */
    private Long creator;

    /**
     * 处理时间
     */
    private Date createTime;

    public BatchRecordPO() {
    }

    public BatchRecordPO(BatchRecord model) {
        id = model.getId();
        dataType = model.getDataType();
        operation = model.getOperation();
        totalNum = model.getTotalNum();
        successNum = model.getSuccessNum();
        failNum = model.getFailNum();
        creator = model.getCreator();
        createTime = model.getCreateTime();
    }

    public BatchRecord toModel() {
        return BatchRecord.builder()
            .id(id)
            .dataType(dataType)
            .operation(operation)
            .totalNum(totalNum)
            .successNum(successNum)
            .failNum(failNum)
            .creator(creator)
            .createTime(createTime)
            .build();
    }


}
