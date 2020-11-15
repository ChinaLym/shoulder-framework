package org.shoulder.batch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 批量执行记录-标准模型
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
     * 导入数据类型，建议可翻译。对应 导入数据库表名
     */
    private String dataType;

    // 目前 不区分操作类型，而是根据 detail 的结果分辨 operationType;

    /**
     * 导入总条数
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
     * 执行导入的用户
     */
    private Long creator;

    /**
     * 导入时间
     */
    private Date createTime;

    /**
     * 关联的详情（每行信息）
     */
    private List<BatchRecordDetail> detailList;

    public BatchRecord() {
    }

    public void addDetail(List<BatchRecordDetail> detailList) {
        if (CollectionUtils.isEmpty(this.detailList)) {
            this.detailList = new ArrayList<>(detailList);
        }
    }

}
