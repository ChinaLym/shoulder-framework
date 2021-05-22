package org.shoulder.batch.dto.result;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.shoulder.batch.enums.ProcessStatusEnum;

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
     * 数据下标 / 所在行数 / 第几个
     */
    @ApiModelProperty(value = "数据下标 / 所在行数 / 第几个", dataType = "Integer", example = "1", position = 1)
    private Integer index;

    /**
     * 结果状态
     */
    @ApiModelProperty(value = "状态: 1校验成功、2校验失败、3导入成功、4导入失败、5重复更新、6重复跳过、7导入校验失败", example = "2")
    private Integer status;

    /**
     * 失败原因（失败详情）
     *
     * @see ProcessStatusEnum
     */
    @ApiModelProperty(value = "失败原因-错误码", example = "用户名已存在", position = 3)
    private String errorCode;

    /**
     * 失败原因参数
     */
    @ApiModelProperty(value = "错误码对应翻译的填充参数", example = "[\"xiaoming\"]",
        position = 4)
    private List<String> reasonParam;

}
