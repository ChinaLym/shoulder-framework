package org.shoulder.web.template.dictionary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 字典
 *
 * @author lym
 */
@Data
public class DictionaryTypeDTO implements Serializable {

    private static final long serialVersionUID = -8214768522392888509L;

    /**
     * 唯一标识符
     */
    @Schema(description = "唯一标识符", example = "123")
    private String id;

    /**
     * 数据版本
     */
    @Schema(description = "数据版本号", example = "1", type = "integer")
    private Integer version;

    /**
     * 字典类型
     */
    @Schema(description = "字典类型标识", example = "GENDER")
    private String code;

    /**
     * 字典展示名称
     */
    @Schema(description = "字典在界面上显示的名称", example = "性别")
    private String displayName;

    /**
     * 字典描述
     */
    @Schema(description = "字典的详细说明或用途", example = "用于表示用户性别", maxLength = 255)
    private String description;

    /**
     * 数据来源
     */
    @Schema(description = "字典数据的来源，如 '系统内置'、'外部接口' 等", example = "SYSTEM")
    private String source;

    /**
     * 是否可修改
     */
    @Schema(description = "是否允许对字典进行修改操作", example = "true", type = "boolean")
    private Boolean modifyAble;

    /**
     * 展示顺序
     */
    @Schema(description = "字典在列表中的展示顺序", example = "1", type = "integer")
    private Integer displayOrder;

    /**
     * 可选项列表
     */
    @Schema(description = "字典项列表，包含具体的可选值及其详细信息")
    private List<DictionaryItemDTO> items;

    public DictionaryTypeDTO() {

    }

    public DictionaryTypeDTO(String code, List<DictionaryItemDTO> items) {
        this.code = code;
        this.items = items;
    }
}
