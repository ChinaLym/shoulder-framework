package org.shoulder.batch.dto.result;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.shoulder.batch.enums.BatchDetailResultStatusEnum;
import org.shoulder.web.template.dictionary.validation.DictionaryEnumItem;
import org.springframework.http.MediaType;

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
@Schema(description = "BatchProcessResult 批量处理结果——查询批量处理结果接口-返回DTO", contentMediaType = MediaType.APPLICATION_JSON_VALUE)
public class BatchProcessResult {

    @Schema(description = "总数", requiredMode = Schema.RequiredMode.REQUIRED, type = "Integer", example = "1")
    private Integer totalNum;

    @Schema(description = "已处理个数", requiredMode = Schema.RequiredMode.REQUIRED, type = "Integer", example = "1")
    private Integer processed;

    @Schema(description = "成功个数", requiredMode = Schema.RequiredMode.NOT_REQUIRED, type = "Integer", example = "1")
    private Integer successNum;

    /**
     * 重复数目未单独作为字段，在 fail 中
     */
    @Schema(description = "失败个数", requiredMode = Schema.RequiredMode.NOT_REQUIRED, type = "Integer", example = "1")
    private Integer failNum;

    @Schema(description = "已执行时间，毫秒", requiredMode = Schema.RequiredMode.REQUIRED, type = "Integer", example = "1234")
    private Long timeConsumed;

    @Schema(description = "预估剩余时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "Integer", example = "1000")
    private Long timeLeft;

    @Schema(description = "是否完成标识", requiredMode = Schema.RequiredMode.REQUIRED, type = "boolean", example = "true")
    private Boolean finish;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, type = "Integer", example = "1")
    @DictionaryEnumItem(value = BatchDetailResultStatusEnum.class)
    private Integer status;

    /**
     * 查进度时暂不返回
     */
    @Schema(description = "处理详情列表(查询进度暂不包含)", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "[{'reason':'reason', row':1,'reasonParam':['xxx']}]")
    @Valid
    private List<BatchProcessDetail> progressList;

}
