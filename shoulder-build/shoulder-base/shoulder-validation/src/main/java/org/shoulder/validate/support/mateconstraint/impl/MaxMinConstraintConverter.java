package org.shoulder.validate.support.mateconstraint.impl;

import org.shoulder.validate.support.mateconstraint.ConstraintConverter;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
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
