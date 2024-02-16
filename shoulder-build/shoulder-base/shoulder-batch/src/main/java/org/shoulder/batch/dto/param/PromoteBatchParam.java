package org.shoulder.batch.dto.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotEmpty;
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
public class PromoteBatchParam {

    @ApiModelProperty(name = "批量操作id", example = "142014201420", dataType = "string", required = true)
    @NotEmpty
    private String batchId;

    @ApiModelProperty(name = "数据类型", example = "user", dataType = "string", required = true)
    @NotEmpty
    private String dataType;

    @ApiModelProperty(name = "当前阶段", example = "validate", dataType = "string", required = true)
    @NotEmpty
    private String currentOperation;

    @ApiModelProperty(name = "下一阶段操作", example = "import", dataType = "string", required = true)
    @NotEmpty
    private String nextOperation;

    @ApiModelProperty(name = "是否更新重复数据", dataType = "java.lang.Boolean", example = "false", required = true)
    private Boolean updateRepeat;
}
