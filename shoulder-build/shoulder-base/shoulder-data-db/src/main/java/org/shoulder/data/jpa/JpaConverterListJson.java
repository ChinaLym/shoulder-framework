package org.shoulder.data.jpa;

import jakarta.persistence.AttributeConverter;
import org.shoulder.core.util.JsonUtils;

/**
 * Json 格式转换器
 *
 * @author lym
 */
public class JpaConverterListJson implements AttributeConverter<Object, String> {

    @Override
    public String convertToDatabaseColumn(Object o) {
        return JsonUtils.toJson(o);
    }

    @Override
    public Object convertToEntityAttribute(String s) {
        return JsonUtils.parseObject(s, Object.class);
    }
}
