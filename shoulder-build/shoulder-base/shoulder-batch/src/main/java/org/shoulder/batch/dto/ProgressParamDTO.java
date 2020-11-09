package org.shoulder.batch.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "查询导入进度参数")
public class ProgressParamDTO {
    @ApiModelProperty(required = true, dataType = "String", value = "批量导入标识", example = "t65b8-e2c9-4x70-8d6c-c4572d88",
            position = 1)
    private String taskId;
}
