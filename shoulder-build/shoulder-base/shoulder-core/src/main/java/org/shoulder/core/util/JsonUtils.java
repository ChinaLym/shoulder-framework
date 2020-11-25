package org.shoulder.core.util;

import cn.hutool.core.io.IoUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.PackageVersion;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.exception.JsonRuntimeException;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
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
     * 语法糖方法，但使用范围受 Java 泛型影响。泛型推断不能推断泛型参数类型，如 List<T>、Map<K, V> 这种
     * 错误使用后果：可能导致 ClassCastException: LinkedHashMap cannot be cast to class xxx
     * 因为本方法签名中返回值的为 T，未指明泛型参数，泛型参数将被抹为 Object，Jackson碰到 Object 将使用 LinkedHashMap
     *
     * @see #toObject(String, TypeReference) 该方法中可以在使用除传入 new TypeReference<>() {} 能利用到java泛型自动推断
     */
    public static <T> T toObject(String json) {
        try {
            return JSON_MAPPER.readValue(json, new TypeReference<>() {
            });
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

    /**
     * 第一次调用时可能较慢（申请堆外内存，加载更多的类）
     */
    public static <T> T toObject(InputStream inputStream, Class<T> clazz, Class<?>... paramClasses) {
        return toObject(IoUtil.read(inputStream, AppInfo.charset()), clazz, paramClasses);
    }

    public static <T> T toObject(InputStream inputStream, TypeReference<T> type) {
        return toObject(IoUtil.read(inputStream, AppInfo.charset()), type);
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

        // 设置为配置中的统一 地区、时区、
        objectMapper.setLocale(AppInfo.defaultLocale());
        objectMapper.setTimeZone(AppInfo.timeZone());

        // 设置序列化日期为配置的统一时间格式
        objectMapper.setDateFormat(new SimpleDateFormat(AppInfo.dateFormat(), AppInfo.defaultLocale()));
        // 反序列化时，允许存在 tab、换行符、结束语符、注释符等控制字符
        objectMapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
        // 反序列化时，可解析反斜杠引用的所有字符
        objectMapper.configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature(), true);

        // 忽略空bean转json错误
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 忽略在json字符串中存在，在java类中不存在字段
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 允许使用单引号代替双引号（更好的兼容性）
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        // 将 key 排序（更好的体验）
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

        if (modifier != null) {
            objectMapper.setSerializerFactory(
                objectMapper.getSerializerFactory().withSerializerModifier(modifier)
            );
        }
        // 添加 jdk8 新增的时间序列化处理模块
        objectMapper.registerModule(new DateEnhancerJacksonModule())
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());

        objectMapper.findAndRegisterModules();
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
     * 解决常见序列化失败问题：java 8 时间、Long 序列化，如果不加入，则可能需要这么写：
     *
     * <code>
     *
     * @author lym
     * @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
     * LocalDateTime time;
     * </code>
     */
    public static class DateEnhancerJacksonModule extends SimpleModule {

        public DateEnhancerJacksonModule() {
            super(PackageVersion.VERSION);

            // 解决 jdk8 日期序列化失败
            String dateFormatStr = "yyyy-MM-dd";
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateFormatStr);
            this.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
            this.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));

            String timeFormatStr = "HH:mm:ss";
            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern(timeFormatStr);
            this.addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormat));
            this.addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormat));

            String datetimeFormatStr = dateFormatStr + " " + timeFormatStr;
            DateTimeFormatter datetimeFormat = DateTimeFormatter.ofPattern(datetimeFormatStr);
            this.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(datetimeFormat));
            this.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(datetimeFormat));

            // 解决 17位+的 Long 给前端导致精度丢失问题，前端将以 str 接收（序列换成json时,将所有的long变成string）
            //this.addSerializer(Long.class, ToStringSerializer.instance);
            //this.addSerializer(Long.TYPE, ToStringSerializer.instance);

        }
    }
}
