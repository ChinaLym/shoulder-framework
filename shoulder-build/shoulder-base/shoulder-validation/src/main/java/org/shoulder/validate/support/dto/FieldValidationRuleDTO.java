package org.shoulder.validate.support.dto;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 字段校验规则
 *
 * @author lym
 */
@Data
@ToString
@Accessors(chain = true)
public class FieldValidationRuleDTO implements Serializable {
    private static final long serialVersionUID = 5909045743743888980L;
    /**
     * bean 字段名称
     */
    private String field;
    /**
     * 字段的类型 （js 的），如 Array、Integer、Float
     */
    private String fieldType;
    /**
     * 约束信息集合
     */
    private List<ConstraintInfoDTO> constraints;
}
