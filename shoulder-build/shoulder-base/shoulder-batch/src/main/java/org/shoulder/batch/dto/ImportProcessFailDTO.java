package org.shoulder.batch.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Objects;

@Api("校验批量新增设备进度失败原因DTO")
@Data
public class ImportProcessFailDTO {

    @ApiModelProperty(value = "失败所在行数", dataType = "Integer", example = "1", position = 1)
    private Integer row;
    @ApiModelProperty(value = "失败原因错误码", dataType = "String", example = "数据已存在", position = 2)
    private String reason;
    @ApiModelProperty(value = "错误码对应翻译的填充参数", dataType = "String", example = "[\"indexCode\"]",
            position = 3)
    private List<String> reasonParam;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImportProcessFailDTO)) {
            return false;
        }
        ImportProcessFailDTO that = (ImportProcessFailDTO) o;
        return Objects.equals(row, that.row) ||
                Objects.equals(reason, that.reason) &&
                        Objects.equals(reasonParam, that.reasonParam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, reason, reasonParam);
    }
}
