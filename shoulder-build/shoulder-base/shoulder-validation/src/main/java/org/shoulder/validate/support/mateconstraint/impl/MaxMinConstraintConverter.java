package org.shoulder.validate.support.mateconstraint.impl;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.shoulder.validate.support.mateconstraint.ConstraintConverter;

import java.lang.annotation.Annotation;
import java.util.Arrays;

/**
 * 长度 转换器
 *
 * @author lym
 */
public class MaxMinConstraintConverter extends BaseConstraintConverter implements ConstraintConverter {

    public MaxMinConstraintConverter() {
        supportAnnotations = Arrays.asList(Max.class, Min.class, DecimalMax.class, DecimalMin.class);
        methods = Arrays.asList("value", "message");
    }

    @Override
    protected String getType(Class<? extends Annotation> type) {
        // such as Max Min DecimalMax DecimalMin
        return type.getSimpleName();
    }

}
