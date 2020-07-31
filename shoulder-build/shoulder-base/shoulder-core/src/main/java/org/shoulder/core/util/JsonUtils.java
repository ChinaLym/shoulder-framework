package org.shoulder.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.PackageVersion;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.shoulder.core.exception.JsonException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * object-json convert
 *
 * @author lym
 */
public class JsonUtils {

    public static final String IGNORE_PROPERTIES = "ignoreProperties";

    private static final ObjectMapper JSON_MAPPER = JacksonFactory.createObjectMapper();

    /**
     * 序列化Object为 JSON 字符串
     *
     * @param object 待序列化对象
     */
    public static String toJson(Object object) {
        try {
            return JSON_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonException(e);
        }
    }

    /**
     * 序列化Object为 JSON 字符串
     *
     * @param object           待序列化对象
     * @param ignoreProperties 忽略的属性名
     */
    public static String toJson(Object object, String... ignoreProperties) {
        ObjectMapper mapper = JacksonFactory.createObjectMapper();
        try {
            return JacksonFactory.setIgnoreFilter(mapper, ignoreProperties).writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonException(e);
        }
    }

    /**
     * 序列化Object为 JSON 字符串
     *
     * @param object           被序列化对象
     * @param modifier         自定义修改器
     * @param ignoreProperties 需要忽略得属性
     */
    public static String toJson(Object object, BeanSerializerModifier modifier, String... ignoreProperties) {
        ObjectMapper mapper = JacksonFactory.createObjectMapper(modifier);
        try {
            return JacksonFactory.setIgnoreFilter(mapper, ignoreProperties).writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonException(e);
        }
    }

    /**
     * 反序列化 JSON 字符串为 Object
     */
    public static <T> T toObject(String json, TypeReference<T> type) {
        try {
            return JSON_MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new JsonException(e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为对象
     *
     * @param json         json字符串
     * @param clazz        反序列化的类型
     * @param paramClasses clazz 类型的泛型类型
     */
    public static <T> T toObject(String json, Class<T> clazz, Class<?>... paramClasses) {
        ObjectMapper mapper = JacksonFactory.createObjectMapper();
        JavaType javaType = mapper.getTypeFactory().constructParametricType(clazz, paramClasses);
        try {
            return mapper.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new JsonException(e);
        }
    }

    /**
     * 解决常见序列化失败问题
     * java 8 时间
     * Long 序列化
     *
     * @author lym
     */
    public class JacksonModule extends SimpleModule {

        public JacksonModule() {
            super(PackageVersion.VERSION);

            // 解决 jdk8 日期序列化失败
            String dateFormat = "yyyy-MM-dd";
            this.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(dateFormat)));
            this.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(dateFormat)));

            String timeFormat = "HH:mm:ss";
            this.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(timeFormat)));
            this.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(timeFormat)));

            String datetimeFormat = dateFormat + " " + timeFormat;
            this.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(datetimeFormat)));
            this.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(datetimeFormat)));

            // 解决 17位+的 Long 给前端导致精度丢失问题，前端将以 str 接收
            this.addSerializer(Long.class, ToStringSerializer.instance);
            this.addSerializer(Long.TYPE, ToStringSerializer.instance);
        }
    }
}
