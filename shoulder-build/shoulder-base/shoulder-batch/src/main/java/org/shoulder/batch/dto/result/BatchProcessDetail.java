package org.shoulder.batch.dto.result;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
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
    @ApiModelProperty(value = "状态: 校验成功、校验失败、导入成功、导入失败、重复更新、重复跳过、导入校验失败...", example = "2")
    @DictionaryEnumItem(value = ProcessStatusEnum.class)
    private Integer status;

    /**
     * 失败原因（失败详情）
     * 当处理失败时才需要展示
     */
    @ApiModelProperty(value = "失败原因-错误码", example = "用户名已存在", position = 3)
    private String errorCode;

    /**
     * 失败原因参数
     * 当处理失败，且失败原因需要参数时才需要
     */
    @ApiModelProperty(value = "错误码对应翻译的填充参数", example = "[\"xiaoming\"]",
        position = 4)
    private List<String> reasonParam;

}
