package org.shoulder.batch.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel(description = "查询批量校验结果接口-返回DTO")
@Data
public class ImportProcessDTO {
    @ApiModelProperty(required = true, value = "总数", dataType = "Integer", example = "1", position = 1)
    private Integer total;
    @ApiModelProperty(required = true, value = "已处理个数", dataType = "Integer", example = "1", position = 2)
    private Integer processed;
    @ApiModelProperty(required = true, value = "已执行时间", dataType = "Integer", example = "3", position = 3)
    private Integer timeProcessed;
    @ApiModelProperty(required = true, value = "预估剩余时间", dataType = "Integer", example = "2", position = 4)
    private Integer timeLeft;
    @ApiModelProperty(required = true, value = "是否完成标识", dataType = "boolean", example = "true", position = 5)
    private Boolean finish;
    @ApiModelProperty(required = true, value = "状态", dataType = "Integer", example = "1", position = 4)
    private Integer status;
    @ApiModelProperty(required = false, value = "失败原因集合", dataType = "list", example = "[{\"reason\":\"reason\"," +
            "\"row\":1,\"reasonParam\":[\"regionIndexCode\"]}]", position = 6)
    private List<ImportProcessFailDTO> list;
}
