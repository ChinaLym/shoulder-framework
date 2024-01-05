package org.shoulder.validate.support.mateconstraint.impl;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.shoulder.validate.support.mateconstraint.ConstraintConverter;

import java.lang.annotation.Annotation;
import java.util.Arrays;

/**
 * 非空 转换器
 *
 * @author lym
 */
public class NotNullConstraintConverter extends BaseConstraintConverter implements ConstraintConverter {

    public NotNullConstraintConverter() {
        supportAnnotations = Arrays.asList(NotNull.class, NotEmpty.class, NotBlank.class);
    }

    @Override
    protected String getType(Class<? extends Annotation> type) {
        return "NotNull";
    }

}
