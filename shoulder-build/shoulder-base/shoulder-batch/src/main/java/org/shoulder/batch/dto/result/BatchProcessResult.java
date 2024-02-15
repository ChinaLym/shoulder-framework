package org.shoulder.batch.dto.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.shoulder.batch.enums.ProcessStatusEnum;
import org.shoulder.web.template.dictionary.validation.DictionaryEnumItem;

import java.util.List;

/**
 * @author lym
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@ApiModel(description = "批量处理结果——查询批量处理结果接口-返回DTO")
public class BatchProcessResult {

    @ApiModelProperty(required = true, value = "总数", dataType = "Integer", example = "1", position = 1)
    private Integer totalNum;

    @ApiModelProperty(required = true, value = "已处理个数", dataType = "Integer", example = "1", position = 2)
    private Integer processed;

    @ApiModelProperty(required = false, value = "成功个数", dataType = "Integer", example = "1", position = 3)
    private Integer successNum;

    /**
     * 重复数目未单独作为字段，在 fail 中
     */
    @ApiModelProperty(required = false, value = "失败个数", dataType = "Integer", example = "1", position = 4)
    private Integer failNum;

    @ApiModelProperty(required = true, value = "已执行时间，毫秒", dataType = "Integer", example = "1234", position = 5)
    private Long timeConsumed;

    @ApiModelProperty(required = true, value = "预估剩余时间", dataType = "Integer", example = "1000", position = 6)
    private Long timeLeft;

    @ApiModelProperty(required = true, value = "是否完成标识", dataType = "boolean", example = "true", position = 7)
    private Boolean finish;

    @ApiModelProperty(required = true, value = "状态", dataType = "Integer", example = "1", position = 8)
    @DictionaryEnumItem(value = ProcessStatusEnum.class)
    private Integer status;

    /**
     * 查进度时暂不返回
     */
    @ApiModelProperty(required = false, value = "处理详情列表", dataType = "list", example = "[{\"reason\":\"reason\"," +
                                                                                             "\"row\":1,\"reasonParam\":[\"xxx\"]}]",
        position = 9)
    @Valid
    private List<BatchProcessDetail> progressList;

}
