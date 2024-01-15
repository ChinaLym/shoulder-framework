package org.shoulder.web.template.dictionary.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 字典项
 *
 * @author lym
 */
@Data
@NoArgsConstructor
public class DictionaryItemDTO implements Serializable, Comparable<DictionaryItemDTO> {

    private static final long serialVersionUID = 1L;

    /**
     * 字典类型，如性别 SEX
     */
    private String dictionaryType;

    /**
     * 字典码，该类型下(前后交互)唯一标记，如 MALE
     */
    private String code;

    /**
     * 字典项的展示名称 页面上显示的内容，如 男性
     */
    private String displayName;

    /**
     * 展示顺序 如 0
     */
    private Integer displayOrder;

    /**
     * 上级节点 如 0
     */
    private String parentCode;

    /**
     * 备注
     */
    private String note;

    @Override
    public int compareTo(DictionaryItemDTO o) {
        return Integer.compare(displayOrder, o.displayOrder);
    }

    public DictionaryItemDTO(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }


}
