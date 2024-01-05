package org.shoulder.web.template.dictionary.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

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
