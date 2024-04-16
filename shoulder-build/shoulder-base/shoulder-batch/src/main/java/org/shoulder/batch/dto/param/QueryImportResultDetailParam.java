package org.shoulder.batch.dto.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;
import org.shoulder.batch.dto.result.BatchRecordResult;
import org.shoulder.core.dto.request.PageQuery;

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
@Schema(description = "QueryImportResultDetailParam 导入记录详情查询条件")
public class QueryImportResultDetailParam extends PageQuery<BatchRecordResult> {

    /**
     * 批量导入id
     */
    @Schema(description = "导入批次id", example = "dqw4244vgr20", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    @Size(max = 128)
    private String batchId;

    /**
     * 业务类型
     */
    @Schema(description = "业务标识", example = "user_add_record", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    @Size(max = 128)
    private String businessType;

    /**
     * 数据状态列表
     * 0 - 1，2，3
     * 1 - 3
     * 2 - 4，5，6，7，8  失败（5，8）成功（4，6，7）
     */
    @Schema(description = "数据状态列表，状态 0-未校验，1-校验通过，2-校验不通过，3、校验重复，4-导入成功，5-导入失败，"
                          + "6-导入重复数据更新，7-导入跳过重复数据，8-导入校验失败", example = "[1,2]", requiredMode = Schema.RequiredMode.REQUIRED,
        type = "java.util.List<java.lang.Integer>")
    private List<Integer> statusList;

    /**
     * @deprecated 使用请求头
     */
    @Schema(description = "文件编码", type = "string", example = "gbk")
    private String charsetLanguage;

}
