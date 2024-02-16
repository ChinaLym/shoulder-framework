package org.shoulder.batch.dto.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

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
@ApiModel(description = "导入记录详情查询条件")
public class QueryImportResultDetailParam {

    /**
     * 批量导入id
     */
    @ApiModelProperty(value = "导入批次id", example = "dqw4244vgr20", dataType = "string", required = true)
    private String batchId;

    /**
     * 业务类型
     */
    @ApiModelProperty(value = "业务标识", example = "user_add_record", dataType = "string", required = true)
    private String businessType;

    /**
     * 数据状态列表
     * 0 - 1，2，3
     * 1 - 3
     * 2 - 4，5，6，7，8  失败（5，8）成功（4，6，7）
     */
    @ApiModelProperty(value = "数据状态列表：数据状态列表:状态 0-未校验，1-校验通过，2-校验不通过，3、校验重复，4-导入成功，5-导入失败，6-导入重复数据更新，7-导入跳过重复数据，8" +
        "-导入校验失败", example = "[1,2]", required = true, dataType = "java.util.List<java.lang.Integer>")
    private List<Integer> statusList;

    /**
     * @deprecated 使用请求头
     */
    @ApiModelProperty(name = "文件编码", dataType = "string", example = "gbk", required = false)
    private String charsetLanguage;

}
