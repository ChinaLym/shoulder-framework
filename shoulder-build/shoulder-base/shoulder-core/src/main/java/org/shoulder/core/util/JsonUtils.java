package org.shoulder.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.PackageVersion;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.shoulder.core.context.ApplicationInfo;
import org.shoulder.core.exception.JsonRuntimeException;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * JSON 和 Object 转换工具类
 * 若要替换使用的 ObjectMapper，只需调用 {@link #setJsonMapper} 或向 spring 容器中注入 ObjectMapper 即可
 *
 * @author lym
 */
public class JsonUtils {

    private static ObjectMapper JSON_MAPPER = createObjectMapper();

    /**
     * 序列化Object为 JSON 字符串
     *
     * @param object 待序列化对象
     */
    public static String toJson(Object object) {
        try {
            return JSON_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonRuntimeException(e);
        }
    }

    /**
     * 序列化Object为 JSON 字符串
     *
     * @param object           待序列化对象
     * @param ignoreProperties 忽略的属性名
     */
    public static String toJson(Object object, String... ignoreProperties) {
        return toJson(object, null, ignoreProperties);
    }
    /**
     * 序列化Object为 JSON 字符串
     *
     * @param object           待序列化对象
     * @param ignoreProperties 忽略的属性名
     */
    public static String toJson(Object object, HashSet<String> ignoreProperties) {
        return toJson(object, null, ignoreProperties);
    }

    /**
     * 序列化Object为 JSON 字符串
     *
     * @param object           被序列化对象
     * @param modifier         自定义修改器
     * @param ignoreProperties 需要忽略得属性
     */
    public static String toJson(Object object, BeanSerializerModifier modifier, String... ignoreProperties) {
        return toJson(object, modifier, new HashSet<>(Arrays.asList(ignoreProperties)));
    }

    /**
     * 序列化Object为 JSON 字符串
     *
     * @param object           被序列化对象
     * @param modifier         自定义修改器
     * @param ignoreProperties 需要忽略得属性
     */
    public static String toJson(Object object, BeanSerializerModifier modifier, HashSet<String> ignoreProperties) {
        final ObjectMapper mapper = JSON_MAPPER.copy();
        try {
            return mapper
                .setSerializerFactory(mapper.getSerializerFactory().withSerializerModifier(modifier))
                .setFilterProvider(createIgnorePropertiesProvider("_temp_ignore", ignoreProperties))
                .writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonRuntimeException(e);
        }
    }

    /**
     * 反序列化 JSON 字符串为 Object
     */
    public static <T> T toObject(String json, TypeReference<T> type) {
        try {
            return JSON_MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new JsonRuntimeException(e);
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
        ObjectMapper mapper = JSON_MAPPER.copy();
        JavaType javaType = mapper.getTypeFactory().constructParametricType(clazz, paramClasses);
        try {
            return mapper.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new JsonRuntimeException(e);
        }
    }

    public static void setJsonMapper(ObjectMapper jsonMapper) {
        LoggerFactory.getLogger(JsonUtils.class).info("JSON_MAPPER changed to " + jsonMapper);
        JSON_MAPPER = jsonMapper;
    }

    // ============================ ObjectMapper 创建 ============================

    public static ObjectMapper createObjectMapper() {
        return createObjectMapper(null);
    }

    public static ObjectMapper createObjectMapper(BeanSerializerModifier modifier) {
        ObjectMapper objectMapper = new ObjectMapper();
        // 设置为配置的时间格式
        objectMapper.setDateFormat(new SimpleDateFormat(ApplicationInfo.dateFormat()));
        // 设置为配置的时区
        objectMapper.setTimeZone(ApplicationInfo.timeZone());
        // 排序key
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        // 忽略空bean转json错误
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 忽略在json字符串中存在，在java类中不存在字段
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        if (modifier != null) {
            objectMapper.setSerializerFactory(objectMapper.getSerializerFactory().withSerializerModifier(modifier));
        }
        // 添加 jdk8 新增的时间序列化处理模块
        objectMapper.registerModule(new DateEnhancerJacksonModule());
        return objectMapper;
    }

    public static ObjectMapper setIgnoreFilter(ObjectMapper mapper, String... properties) {
        mapper.setFilterProvider(createIgnorePropertiesProvider("_temp_ignore", new HashSet<>(Arrays.asList(properties))));
        return mapper;
    }

    public static SimpleFilterProvider createIgnorePropertiesProvider(String filterName, Set<String> ignores) {
        return new SimpleFilterProvider().addFilter(
            filterName, SimpleBeanPropertyFilter.serializeAllExcept(ignores));
    }

    /**
     * 解决常见序列化失败问题
     * java 8 时间
     * Long 序列化
     *
     * @author lym
     */
    public static class DateEnhancerJacksonModule extends SimpleModule {

        public DateEnhancerJacksonModule() {
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

            // 解决 17位+的 Long 给前端导致精度丢失问题，前端将以 str 接收（序列换成json时,将所有的long变成string）
            //this.addSerializer(Long.class, ToStringSerializer.instance);
            //this.addSerializer(Long.TYPE, ToStringSerializer.instance);

        }
    }
}
