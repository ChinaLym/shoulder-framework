package org.shoulder.validate.util;

import org.apache.commons.collections4.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validation;
import java.util.Set;

/**
 * @author lym
 */
public class ValidateUtil {

    private static final javax.validation.Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * validate主动校验方式
     *
     * @param validateObject 进行校验的对象
     * @param <T>            传递的校验类型
     */
    public static <T> void validate(@Valid T validateObject) throws ConstraintViolationException {
        Set<ConstraintViolation<T>> constraintViolationSet = VALIDATOR.validate(validateObject);
        if (CollectionUtils.isNotEmpty(constraintViolationSet)) {
            throw new ConstraintViolationException(constraintViolationSet);
        }
    }

}