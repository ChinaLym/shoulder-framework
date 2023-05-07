package org.shoulder.validate.validator;

import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.StringUtils;
import org.shoulder.validate.annotation.JsonType;
import org.shoulder.validate.annotation.JsonType.FormType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;

/**
 * {@link JsonType} 注解校验处理，支持数组
 *
 * @author lym
 * @see JsonType
 */
public class JsonTypeValidator implements ConstraintValidator<JsonType, String> {

    protected FormType jsonType = null;

    @Override
    public void initialize(JsonType jsonType) {
        this.jsonType = jsonType.formType();
    }

    @Override
    public boolean isValid(String input, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(input)) {
            return true;
        }
        try {
//            if (jsonType == JsonType.FormType.DEFAULT) {
//                JsonUtils.parseObject(input);
//            } else
            if (jsonType == JsonType.FormType.OBJECT) {
                JsonUtils.parseObject(input, Object.class);
            } else if (jsonType == JsonType.FormType.MAP) {
                JsonUtils.parseObject(input, Map.class);
            } else if (jsonType == JsonType.FormType.LIST) {
                JsonUtils.parseObject(input, List.class);
            }
            JsonUtils.parseObject(input, Map.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}


