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
     * 字典码，该类型下(前后交互)唯一标记，如 MALE 可用 1、M、MALE 表示
     */
    private String code;
    /**
     * name todo 用于提示代码，如 MALE，通常和代码中名字相关方便搜索
     */
    private String name;

    /**
     * 字典项的展示名称 页面上显示的内容，如 男性 或者前端国际化的可以是 i18nKey
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
