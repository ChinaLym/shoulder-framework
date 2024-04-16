package org.shoulder.batch.dto.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;

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
@Schema(description = "AdvanceBatchParam 触发操作参数", contentMediaType = MediaType.APPLICATION_JSON_VALUE)
public class AdvanceBatchParam {

    @Schema(description = "批处理操作id", example = "142014201420", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String batchId;

    @Schema(description = "数据类型", example = "user", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String dataType;

    @Schema(description = "当前阶段的操作名称", example = "validate", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String currentOperation;

    @Schema(description = "下一阶段的操作名称", example = "import", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String nextOperation;

    @Schema(description = "在遇到数据重复时，是否用最新数据更新重复的数据", type = "java.lang.Boolean", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean updateRepeat;
}
