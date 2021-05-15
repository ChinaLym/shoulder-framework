package org.shoulder.validate.support.mateconstraint.impl;

import org.shoulder.validate.support.mateconstraint.ConstraintConverter;

import java.lang.annotation.Annotation;
import java.util.List;

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
    protected List<Class<? extends Annotation>> getSupportAnnotations() {
        return SUPPORT_ALL;
    }

}
