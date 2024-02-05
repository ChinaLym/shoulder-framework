package org.shoulder.web.template.dictionary.dto;

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

    private String id;

    private Integer version;

    /**
     * 字典类型
     */
    private String code;

    /**
     * 字典展示名称
     */
    private String displayName;

    private String description;

    private String source;

    private Boolean addItemAble;

    private Integer displayOrder;

    /**
     * 可选项
     */
    private List<DictionaryItemDTO> items;

    public DictionaryTypeDTO() {

    }

    public DictionaryTypeDTO(String code, List<DictionaryItemDTO> items) {
        this.code = code;
        this.items = items;
    }
}
