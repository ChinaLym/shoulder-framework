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
public class BatchRecordDetailResult {

    /**
     * 行数
     */
    @ApiModelProperty(required = true, value = "数据所在行数", dataType = "Integer", example = "1", position = 1)
    private Integer rowNum;

    /**
     * 结果状态 0 处理成功 1 校验失败、2 重复跳过、3 重复更新、4 处理失败
     */
    @ApiModelProperty(required = true, value = "状态: 1校验成功、2校验失败、3导入成功、4导入失败、5重复更新、6重复跳过、7导入校验失败",
        example = "1", position = 2)
    private int status;

    /**
     * 失败原因
     *
     * @see BatchResultEnum
     */
    @ApiModelProperty(value = "失败原因:错误码", dataType = "String", example = "用户名已存在", position = 3)
    private String reason;

    /**
     * 用于填充翻译项
     */
    @ApiModelProperty(value = "错误码对应翻译的填充参数", dataType = "String", example = "[\"xiaoming\"]",
        position = 4)
    private List<String> reasonParam;

    /**
     * 处理的原始数据
     */
    @ApiModelProperty(required = true, value = "失败原因:错误码", dataType = "String", example = "用户名已存在", position = 5)
    private String source;


}
