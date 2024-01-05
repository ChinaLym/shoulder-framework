package org.shoulder.validate.support.mateconstraint.impl;

import jakarta.validation.Constraint;
import org.shoulder.validate.support.mateconstraint.ConstraintConverter;

import java.lang.annotation.Annotation;

/**
 * 其他 转换器
 *
 * @author lym
 */
public class OtherConstraintConverter extends BaseConstraintConverter implements ConstraintConverter {

    @Override
    protected String getType(Class<? extends Annotation> type) {
        return type.getSimpleName();
    }

    @Override
    public boolean support(Class<? extends Annotation> annotationClass) {
        return annotationClass != null && annotationClass.getAnnotation(Constraint.class) != null;
    }

}
