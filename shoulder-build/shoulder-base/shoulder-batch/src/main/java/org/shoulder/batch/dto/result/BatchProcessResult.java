package org.shoulder.batch.dto.result;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.Accessors;
import org.shoulder.batch.enums.BatchDetailResultStatusEnum;
import org.shoulder.web.template.dictionary.validation.DictionaryEnumItem;

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
@Schema(name = "BatchProcessResult 批量处理结果——查询批量处理结果接口-返回DTO")
public class BatchProcessResult {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, name = "总数", type = "Integer", example = "1")
    private Integer totalNum;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, name = "已处理个数", type = "Integer", example = "1")
    private Integer processed;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, name = "成功个数", type = "Integer", example = "1")
    private Integer successNum;

    /**
     * 重复数目未单独作为字段，在 fail 中
     */
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, name = "失败个数", type = "Integer", example = "1")
    private Integer failNum;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, name = "已执行时间，毫秒", type = "Integer", example = "1234")
    private Long timeConsumed;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, name = "预估剩余时间", type = "Integer", example = "1000")
    private Long timeLeft;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, name = "是否完成标识", type = "boolean", example = "true")
    private Boolean finish;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, name = "状态", type = "Integer", example = "1")
    @DictionaryEnumItem(value = BatchDetailResultStatusEnum.class)
    private Integer status;

    /**
     * 查进度时暂不返回
     */
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, name = "处理详情列表(查询进度暂不包含)", example = "[{'reason':'reason', row':1,'reasonParam':['xxx']}]")
    @Valid
    private List<BatchProcessDetail> progressList;

}
