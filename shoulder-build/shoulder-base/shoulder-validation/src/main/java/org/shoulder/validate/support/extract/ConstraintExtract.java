package org.shoulder.validate.support.extract;

import jakarta.annotation.Nonnull;
import org.shoulder.validate.support.dto.FieldValidationRuleDTO;
import org.shoulder.validate.support.model.ValidConstraint;

import java.util.List;


/**
 * 表单验证规则提取器
 *
 * @author lym
 */
public interface ConstraintExtract {

    /**
     * 提取指定表单验证规则
     *
     * @param constraints 限制条件
     * @return 验证规则
     * @throws Exception 异常
     */
    List<FieldValidationRuleDTO> extract(@Nonnull List<ValidConstraint> constraints) throws Exception;
}
