package org.shoulder.web.template.dictionary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

import java.io.Serializable;

/**
 * 字典项
 *
 * @author lym
 */
@Data
@NoArgsConstructor
@Schema(description = "字典项", contentMediaType = MediaType.APPLICATION_JSON_VALUE)
public class DictionaryItemDTO implements Serializable, Comparable<DictionaryItemDTO> {

    private static final long serialVersionUID = 1L;
    /**
     * 字典类型，如性别 SEX
     */
    @Schema(description = "字典类型标识，如性别（SEX）", example = "SEX", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictionaryType;

    /**
     * 字典码，该类型下(前后交互)唯一标记，如 MALE 可用 1、M、MALE 表示
     */
    @Schema(description = "字典项唯一标识码，如 MALE（可用 1、M、MALE 表示）", example = "MALE")
    private String code;

    /**
     * name todo 用于提示代码，如 MALE，通常和代码中名字相关方便搜索
     */
    @Schema(description = "代码提示名，与代码相关便于搜索，如 MALE", example = "MALE")
    private String name;

    /**
     * 字典项的展示名称 页面上显示的内容，如 男性 或者前端国际化的可以是 i18nKey
     */
    @Schema(description = "字典项在界面上显示的名称，如 男性 或者前端国际化的键（i18nKey）", example = "男性")
    private String displayName;

    /**
     * 展示顺序 如 0
     */
    @Schema(description = "字典项在列表中的展示顺序，如 0", example = "0")
    private Integer displayOrder;

    /**
     * 上级节点 如 0
     */
    @Schema(description = "上级字典项标识码，如 0（表示顶级节点）", example = "0")
    private String parentCode;

    /**
     * 备注
     */
    @Schema(description = "字典项备注信息", example = "用于描述男性的字典项", maxLength = 255)
    private String description;

    @Override
    public int compareTo(DictionaryItemDTO o) {
        return Integer.compare(displayOrder, o.displayOrder);
    }

    public DictionaryItemDTO(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }


}
