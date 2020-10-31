package org.shoulder.data.jpa;

import org.shoulder.core.util.JsonUtils;

import javax.persistence.AttributeConverter;

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
        return JsonUtils.toObject(s, Object.class);
    }
}
