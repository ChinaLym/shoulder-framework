package org.shoulder.batch.dto.result;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "BatchProcessDetail 批量处理详情", contentMediaType = MediaType.APPLICATION_JSON_VALUE)
public class BatchProcessDetail {

    /**
     * 数据下标 / 所在行数 / 第几个
     */
    @Schema(description = "数据下标 / 所在行数 / 第几个", type = "Integer", example = "1")
    private Integer index;

    /**
     * 结果状态
     */
    @Schema(description = "状态: 校验成功、校验失败、导入成功、导入失败、重复更新、重复跳过、导入校验失败...", example = "2")
    @DictionaryEnumItem(value = BatchDetailResultStatusEnum.class)
    private Integer status;

    /**
     * 失败原因（失败详情）
     * 当处理失败时才需要展示
     */
    @Schema(description = "失败原因-错误码", example = "用户名已存在")
    private String errorCode;

    /**
     * 失败原因参数
     * 当处理失败，且失败原因需要参数时才需要
     */
    @Schema(description = "错误码对应翻译的填充参数", example = "['xiaoming']")
    private List<String> reasonParam;

}
