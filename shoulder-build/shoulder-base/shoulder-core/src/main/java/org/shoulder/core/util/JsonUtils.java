package org.shoulder.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import org.shoulder.core.exception.JsonException;

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
	 * @param object 待序列化对象
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
	 * @param object 被序列化对象
	 * @param modifier  自定义修改器
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
     * @param json json字符串
     * @param clazz 反序列化的类型
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

}
