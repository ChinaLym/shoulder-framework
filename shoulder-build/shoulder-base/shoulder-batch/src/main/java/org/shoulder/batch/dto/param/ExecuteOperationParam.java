package org.shoulder.batch.dto.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

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
@ApiModel(description = "触发操作参数")
public class ExecuteOperationParam {

    @ApiModelProperty(name = "批量操作id", example = "142014201420", dataType = "string", required = true)
    private String taskId;

    @ApiModelProperty(name = "是否更新重复数据", dataType = "java.lang.Boolean", example = "false", required = true)
    private Boolean updateRepeat;
}
