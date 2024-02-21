package org.shoulder.batch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.shoulder.batch.spi.DataItem;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * 批量处理记录详情-标准模型
 *
 * @author lym
 */
@Data
@Builder
@Accessors(chain = true)
@AllArgsConstructor
public class BatchRecordDetail implements Serializable, DataItem {

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
     * <br><strong>需要 Handler 补充</strong>
     */
    private int index;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 处理结果状态 0 处理成功 1 校验失败、2 重复跳过、3 重复更新、4 处理失败
     * <br><strong>需要 Handler 补充</strong>
     */
    private int status;

    /**
     * 失败原因，推荐支持多语言
     * <br><strong>需要 Handler 补充</strong>
     */
    private String failReason;

    /**
     * 失败原因-翻译参数
     */
    private List<String> failReasonParams;

    /**
     * 处理的原始数据
     */
    private String source;

    /**
     * 是否计算进度
     */
//    private boolean calculateProgress = true;

    public BatchRecordDetail() {
    }

    public BatchRecordDetail(int index, String source, int status) {
        this.index = index;
        this.source = source;
        this.status = status;
    }

    public BatchRecordDetail(int index, String source, int status, String failReason, String... failReasonParams) {
        this.index = index;
        this.source = source;
        this.status = status;
        this.failReason = failReason;
        if(failReasonParams != null) {
            this.failReasonParams = Arrays.stream(failReasonParams).toList();
        }
    }

    @Override
    public String serialize() {
        return source;
    }
}
