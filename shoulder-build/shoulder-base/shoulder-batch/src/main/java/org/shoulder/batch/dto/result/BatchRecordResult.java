package org.shoulder.batch.dto.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
@ApiModel(description = "批量处理结果——查询批量处理结果接口-返回DTO")
public class BatchRecordResult extends ToStringObj {

    @ApiModelProperty(required = true, value = "批次id", dataType = "String", example = "aDemoBatchId", position = 1)
    private String batchId;


    /**
     * 操作数据类型，建议可翻译。如对应 导入数据库表名、业务线名，例：用户、用户组、推广消息
     */
    private String dataType;

    /**
     * 操作类型，建议可翻译。如对应 动作名称，例：导入、同步、推送、下载
     */
    private String operation;

    @ApiModelProperty(required = true, value = "总数", dataType = "Integer", example = "1", position = 1)
    private Integer totalNum;

    @ApiModelProperty(required = true, value = "成功个数", dataType = "Integer", example = "1", position = 3)
    private Integer successNum;

    /**
     * 失败数目（包括重复跳过等）
     */
    @ApiModelProperty(required = true, value = "失败个数", dataType = "Integer", example = "1", position = 4)
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
    @ApiModelProperty(value = "操作详情项列表", dataType = "list", example = "[{}]", position = 9)
    private List<BatchRecordDetailResult> detailList;

}
