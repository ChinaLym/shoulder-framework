package org.shoulder.validate.util;

import com.google.common.collect.Lists;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.util.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * @author lym
 */
public class ValidateUtil {

    private static final jakarta.validation.Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

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

    /**
     * 将检查异常转为 string
     *
     * @param e e
     * @return string
     */
    public static String toValidationStr(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        List<String> messages = Lists.newArrayList();
        for (ConstraintViolation<?> constraintViolation : constraintViolations) {
            messages.add(constraintViolation.getPropertyPath().toString() + " " + constraintViolation.getMessage());
        }
        return StringUtils.join(messages, ",");
    }

}
