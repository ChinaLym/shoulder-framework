package org.shoulder.batch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.batch.repository.po.BatchRecordPO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 批量处理记录-标准模型
 *
 * @author lym
 */
@Data
@Builder
@AllArgsConstructor
public class BatchRecord implements Serializable {

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

    /**
     * 关联的详情（每行信息）
     */
    private List<BatchRecordDetail> detailList;

    public BatchRecord() {
    }

    public BatchRecord(BatchRecordPO po) {
        id = po.getId();
        dataType = po.getDataType();
        operation = po.getOperation();
        totalNum = po.getTotalNum();
        successNum = po.getSuccessNum();
        failNum = po.getFailNum();
        creator = po.getCreator();
        createTime = po.getCreateTime();
    }

    public BatchRecordPO toPersistent() {
        return BatchRecordPO.builder()
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

    public void addDetail(List<BatchRecordDetail> detailList) {
        if (CollectionUtils.isEmpty(this.detailList)) {
            this.detailList = new ArrayList<>(detailList);
        }
    }

}
