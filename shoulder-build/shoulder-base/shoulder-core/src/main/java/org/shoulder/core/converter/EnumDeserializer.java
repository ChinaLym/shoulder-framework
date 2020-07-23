package org.shoulder.core.converter;

import cn.hutool.core.util.ReflectUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.shoulder.core.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 枚举反序列化工具，用于 Controller 接口用枚举接收字符串
 * 需要约定，通过 get 方法
 *
 * @author lym
 */
@Slf4j
public class EnumDeserializer extends StdDeserializer<Enum<?>> {

    /**
     * 约定方法名，当且仅当枚举中存在
     * <code>public static Enum from(String str)</code> Enum代表自身
     * 的方法时，才能转换
     */
    private final String stringToEnumMethodName;

    private final String indexFieldName;

    public EnumDeserializer() {
        this(null, null);
    }

    public EnumDeserializer(String stringToEnumMethodName, String indexFieldName) {
        super(Enum.class);
        String defaultStringToEnumMethodName = "from";
        String defaultIndexFieldName = "code";
        this.stringToEnumMethodName = StringUtils.isEmpty(stringToEnumMethodName) ? defaultStringToEnumMethodName : stringToEnumMethodName;
        this.indexFieldName = StringUtils.isEmpty(stringToEnumMethodName) ? defaultIndexFieldName : indexFieldName;
    }

    @Override
    public Enum<?> deserialize(JsonParser p, DeserializationContext context) throws IOException {
        JsonToken token = p.getCurrentToken();
        String value = null;
        while (!token.isStructEnd()) {
            if (indexFieldName.equals(p.getText())) {
                p.nextToken();
                value = p.getValueAsString();
            } else {
                // 下一个 key 或者 value
                p.nextToken();
            }
            token = p.getCurrentToken();
        }
        // 没匹配到
        if (value == null || "".equals(value)) {
            return null;
        }

        Object obj = p.getCurrentValue();
        if (obj == null) {
            return null;
        }
        Field field = ReflectUtil.getField(obj.getClass(), p.getCurrentName());
        //找不到字段
        if (field == null) {
            return null;
        }
        Class<?> fieldType = field.getType();
        try {
            Method method = fieldType.getMethod(stringToEnumMethodName, String.class);
            return (Enum<?>) method.invoke(null, value);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
            log.warn("Deserialize enum fail! Can't invoke the method named  '" + stringToEnumMethodName  + "'", e);
            return null;
        }
    }

}


