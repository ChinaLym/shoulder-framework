package org.shoulder.batch.dto.result;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;
import org.shoulder.core.dto.ToStringObj;

import java.util.Date;
import java.util.List;

/**
 * @author lym
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Schema(name = "BatchRecordResult 批量处理结果——查询批量处理结果接口-返回DTO")
public class BatchRecordResult extends ToStringObj {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, name = "批次id", type = "String", example = "aDemoBatchId")
    private String batchId;


    /**
     * 操作数据类型，建议可翻译。如对应 导入数据库表名、业务线名，例：用户、用户组、推广消息
     */
    private String dataType;

    /**
     * 操作类型，建议可翻译。如对应 动作名称，例：导入、同步、推送、下载
     */
    private String operation;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, name = "总数", type = "Integer", example = "1")
    private Integer totalNum;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, name = "成功个数", type = "Integer", example = "1")
    private Integer successNum;

    /**
     * 失败数目（包括重复跳过等）
     */
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, name = "失败个数", type = "Integer", example = "1")
    private Integer failNum;

    /**
     * 操作用户
     */
    private Long operator;

    /**
     * 处理时间
     */
    private Date executedTime;

    /**
     * 操作详情项
     */
    @Schema(name = "操作详情项列表", type = "list", example = "[{}]")
    private List<BatchRecordDetailResult> detailList;

}
