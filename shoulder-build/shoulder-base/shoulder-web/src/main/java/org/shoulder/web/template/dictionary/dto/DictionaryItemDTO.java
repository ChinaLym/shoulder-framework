package org.shoulder.web.template.dictionary.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 字典
 *
 * @author lym
 */
@Data
public class DictionaryItemDTO implements Serializable {

    private static final long serialVersionUID = 1406811097279303491L;

    /**
     * 字典项名称
     */
    private String name;

    /**
     * 展示名称
     */
    private String displayName;

    public DictionaryItemDTO() {
    }

    public DictionaryItemDTO(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

}
