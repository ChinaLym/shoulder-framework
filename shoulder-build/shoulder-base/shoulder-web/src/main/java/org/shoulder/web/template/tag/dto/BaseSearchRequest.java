package org.shoulder.web.template.tag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.MediaType;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@ToString
@Schema(description = "标签查询请求", contentMediaType = MediaType.APPLICATION_JSON_VALUE)
public class BaseSearchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 业务类型
     */
    @Schema(description = "业务类型标识", example = "USER")
    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    /**
     * 搜索框输入的内容
     * 若为空则查全部
     * 不为空作为模糊搜索条件
     */
    @Schema(description = "搜索框输入的文字内容", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "搜索内容不能为空")
    private String searchContent;

    /**
     * 业务上过滤条件
     */
    @Schema(description = "业务过滤条件键值对", example = "{'status': 'ACTIVE', 'category': 'BOOK'}")
    private Map<String, String> condition;

    /**
     * 返回结果数量限制
     */
    @Schema(description = "返回结果的最大数量限制，取值范围：1-1000", example = "50", minimum = "1", maximum = "1000", type = "integer")
    @DecimalMax(value = "1000", message = "limit 值不能超过 1000")
    private Integer limit;
}
