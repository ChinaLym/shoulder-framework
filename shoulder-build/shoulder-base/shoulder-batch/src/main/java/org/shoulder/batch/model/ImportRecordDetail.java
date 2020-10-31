package org.shoulder.batch.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 导入记录详情
 *
 * @author lym
 */
@Data
@NoArgsConstructor
public class ImportRecordDetail implements Serializable {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 导入记录表id
     */
    private Integer recordId;

    /**
     * 结果 0 导入成功 1 校验失败、2 重复跳过、3 重复更新、4 导入失败
     */
    private Integer result;

    /**
     * 导入行号
     */
    private Integer line;

    /**
     * 失败原因，推荐支持多语言
     */
    private String failReason;

    /**
     * 导入的原数据
     */
    private String source;

}
