package org.shoulder.core.converter.jackson;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.shoulder.core.converter.EnumConverterFactory;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.springframework.beans.BeanUtils;

/**
 * jackson enum 反序列化
 *
 * @author lym
 * @deprecated jackson自身就支持
 */
public class ShoulderEnumDeserializer extends StdDeserializer<Enum<?>> {

    private static final Logger log = LoggerFactory.getLogger(ShoulderEnumDeserializer.class);

    public static final ShoulderEnumDeserializer INSTANCE = new ShoulderEnumDeserializer();

    private ShoulderEnumDeserializer() {
        super(Enum.class);
    }

    @SuppressWarnings("rawtypes, unchecked")
    @Override
    public Enum<?> deserialize(JsonParser jp, DeserializationContext context) {
        try {
            JsonNode node = jp.getCodec().readTree(jp);
            String currentName = jp.currentName();
            // 当前对象
            Object currentValue = jp.getCurrentValue();
            // 在对象中找到该字段
            Class enumClass = BeanUtils.findPropertyType(currentName, currentValue.getClass());
            String source = node.asText();
            if (StrUtil.isBlank(source) || "null".equals(source)) {
                return null;
            }
            return (Enum<?>) EnumConverterFactory.getDefaultInstance()
                    .getConverter(enumClass).convert(source);
        } catch (Exception e) {
            log.warn("parse FAIL", e);
            return null;
        }
    }


}
