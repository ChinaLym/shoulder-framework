package org.shoulder.web.template.dictionary.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 批量查询参数
 *
 * @author lym
 */
@Data
public class DictionaryBatchQueryParam implements Serializable {

    private static final long serialVersionUID = 2773729771586823614L;

    /**
     * 字典类型
     */
    @NotNull
    @Size(max = 20)
    List<String> dictionaryTypeList;

}
