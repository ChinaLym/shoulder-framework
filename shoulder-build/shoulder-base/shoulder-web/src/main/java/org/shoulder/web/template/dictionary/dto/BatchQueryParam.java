package org.shoulder.web.template.dictionary.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 批量查询参数
 *
 * @author lym
 */
@Data
public class BatchQueryParam implements Serializable {

    private static final long serialVersionUID = 2773729771586823614L;

    /**
     * 字典类型
     */
    @NotNull
    List<String> dictionaryTypeList;

}
