package org.shoulder.batch.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "批量添加设备返回结果数量Dto")
public class AddBatchResultNumDTO {

    @ApiModelProperty(required = false, value = "总数", dataType = "Integer", example = "1", position = 1)
    private Integer total;
    @ApiModelProperty(required = false, value = "成功个数", dataType = "Integer", example = "1", position = 2)
    private Integer successNum;
    @ApiModelProperty(required = false, value = "失败个数", dataType = "Integer", example = "1", position = 2)
    private Integer failNum;
}
