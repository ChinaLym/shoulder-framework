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
public class DictionaryDTO implements Serializable {

    private static final long serialVersionUID = -8214768522392888509L;

    /**
     * 字典类型
     */
    private String dictionaryType;

    /**
     * 字典展示名称
     */
    private String displayName;

    /**
     * 可选项
     */
    private List<DictionaryItemDTO> items;

    public DictionaryDTO() {

    }

    public DictionaryDTO(String dictionaryType, List<DictionaryItemDTO> items) {
        this.dictionaryType = dictionaryType;
        this.items = items;
    }
}
