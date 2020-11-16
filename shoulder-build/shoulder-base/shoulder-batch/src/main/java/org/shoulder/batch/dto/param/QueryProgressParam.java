package org.shoulder.batch.dto.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lym
 */
@Data
@ApiModel(description = "查询批处理进度参数")
public class QueryProgressParam {

    @ApiModelProperty(required = true, dataType = "String", value = "任务标识", example = "t65b8-e2c9-4x70-8d6c-c4572d88",
        position = 1)
    private String taskId;

}
