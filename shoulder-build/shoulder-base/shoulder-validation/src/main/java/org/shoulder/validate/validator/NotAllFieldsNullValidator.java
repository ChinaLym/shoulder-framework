package org.shoulder.validate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.shoulder.validate.annotation.NotAllFieldsNull;

/**
 * 判断多个字段不能同时为空
 *
 * @author lym
 */
public class NotAllFieldsNullValidator implements ConstraintValidator<NotAllFieldsNull, Object> {

    /**
     * 这几个字段不能同时为空（至少有一个非空）
     */
    private String[] fields;

    @Override
    public void initialize(NotAllFieldsNull constraintAnnotation) {
        fields = constraintAnnotation.fields();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        int i = 0;
        for (String field : fields) {
            //getMethod获取包括继承的公共方法
            try {
                Object fieldValue = value.getClass().getMethod(generateGetterMethod(field)).invoke(value);
                if (fieldValue != null) {
                    i += 1;
                }
            } catch (ReflectiveOperationException e) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "@NotAllFieldsNull annotation parameter fields.[" + field + "] is illegal").addPropertyNode(field)
                    .addConstraintViolation();
                return false;
            }
        }

        if (i == 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("at least one fields should be not null")
                .addPropertyNode(fields[0]).addConstraintViolation();
            return false;
        }

        return true;
    }

    private String generateGetterMethod(String fieldName) {
        char[] ch = fieldName.toCharArray();
        if (ch[0] >= 'a' && ch[0] <= 'z') {
            ch[0] = (char) (ch[0] - 32);
        }
        return "get" + new String(ch);
    }
}
