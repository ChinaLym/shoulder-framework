package org.shoulder.batch.dto.result;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.shoulder.batch.enums.BatchResultEnum;

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
@Api("批量处理详情")
public class BatchProcessDetail {

    /**
     * 行数
     */
    @ApiModelProperty(value = "数据所在行数", dataType = "Integer", example = "1", position = 1)
    private Integer row;

    /**
     * 结果状态
     */
    @ApiModelProperty(value = "状态: 1校验成功、2校验失败、3导入成功、4导入失败、5重复更新、6重复跳过、7导入校验失败", example = "1")
    private Integer status;

    /**
     * 失败原因
     *
     * @see BatchResultEnum
     */
    @ApiModelProperty(value = "失败原因-错误码", dataType = "String", example = "用户名已存在", position = 2)
    private String reason;

    @ApiModelProperty(value = "错误码对应翻译的填充参数", dataType = "String", example = "[\"xiaoming\"]",
        position = 3)
    private List<String> reasonParam;

}
