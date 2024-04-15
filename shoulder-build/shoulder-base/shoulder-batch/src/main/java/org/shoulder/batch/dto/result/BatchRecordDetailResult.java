package org.shoulder.batch.dto.result;


import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "BatchRecordDetailResult 批量处理详情")
public class BatchRecordDetailResult {

    /**
     * 行数
     */
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, name = "数据所在行数", type = "Integer", example = "1")
    private int index;

    /**
     * 结果状态 0 处理成功 1 校验失败、2 重复跳过、3 重复更新、4 处理失败
     */
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, name = "状态: 1校验成功、2校验失败、3导入成功、4导入失败、5重复更新、6重复跳过、7导入校验失败",
            example = "1")
    @DictionaryEnumItem(value = BatchDetailResultStatusEnum.class)
    private int status;

    /**
     * 失败原因
     *
     * @see org.shoulder.batch.enums.BatchI18nEnum
     */
    @Schema(name = "失败原因:错误码", example = "用户名已存在")
    private String reason;

    /**
     * 用于填充翻译项
     */
    @Schema(name = "错误码对应翻译的填充参数", example = "[\"xiaoming\"]")
    private List<String> reasonParam;

    /**
     * 处理的原始数据
     */
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, name = "失败原因:错误码", example = "用户名已存在")
    private String source;


}
