package org.shoulder.web.template.dictionary.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 字典
 *
 * @author lym
 */
@Data
@NoArgsConstructor
public class DictionaryItemDTO implements Serializable, Comparable<DictionaryItemDTO> {

    private static final long serialVersionUID = 1L;

    /**
     * 字典码
     * * CASHIER
     * 前后交互的值
     */
    private String code;

    /**
     * 字典类型
     * * product
     */
    private String bizType;

    /**
     * 展示名称
     * * 收银台
     * 页面山显示的内容
     */
    private String displayName;

    /**
     * 展示顺序
     */
    private Integer displayOrder;

    /**
     * 上级节点
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
