package org.shoulder.batch.dto.param;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "AdvanceBatchParam 触发操作参数")
public class AdvanceBatchParam {

    @Schema(name = "批量操作id", example = "142014201420", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String batchId;

    @Schema(name = "数据类型", example = "user", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String dataType;

    @Schema(name = "当前阶段", example = "validate", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String currentOperation;

    @Schema(name = "下一阶段操作", example = "import", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String nextOperation;

    @Schema(name = "是否更新重复数据", type = "java.lang.Boolean", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean updateRepeat;
}
