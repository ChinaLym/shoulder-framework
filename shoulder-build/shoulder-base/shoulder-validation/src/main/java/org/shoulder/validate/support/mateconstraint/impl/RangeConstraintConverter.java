package org.shoulder.validate.support.mateconstraint.impl;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.shoulder.validate.support.mateconstraint.ConstraintConverter;

import javax.validation.constraints.Size;
import java.lang.annotation.Annotation;
import java.util.Arrays;

/**
 * 长度 转换器
 *
 * @author lym
 */
public class RangeConstraintConverter extends BaseConstraintConverter implements ConstraintConverter {

    public RangeConstraintConverter() {
        supportAnnotations = Arrays.asList(Length.class, Size.class, Range.class);
        methods = Arrays.asList("min", "max", "message");
    }

    @Override
    protected String getType(Class<? extends Annotation> type) {
        return "Range";
    }

}
