package org.shoulder.batch.dto.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.http.MediaType;

/**
 * @author lym
 */
@Data
@Schema(description = "QueryProgressParam 查询批处理进度参数", contentMediaType = MediaType.APPLICATION_JSON_VALUE)
public class QueryProgressParam {

    @Schema(description = "批处理任务id", requiredMode = Schema.RequiredMode.REQUIRED, example = "t65b8-e2c9-4x70-8d6c-c4572d88")
    private String batchId;

}
