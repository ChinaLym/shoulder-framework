package org.shoulder.core.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Jackson 创建工厂类
 *
 * @author lym
 */
public class JacksonFactory {

    public static ObjectMapper createObjectMapper() {
        return createObjectMapper(null);
    }

    public static ObjectMapper createObjectMapper(BeanSerializerModifier modifier) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        if (modifier != null) {
            mapper.setSerializerFactory(mapper.getSerializerFactory().withSerializerModifier(modifier));
        }
        return mapper;
    }

    public static ObjectMapper setIgnoreFilter(ObjectMapper mapper, String... properties) {
        mapper.setFilterProvider(createIgnorePropertiesProvider(new HashSet<>(Arrays.asList(properties))));
        return mapper;
    }

    private static FilterProvider createIgnorePropertiesProvider(Set<String> ignores) {
        return new SimpleFilterProvider().addFilter(
            JsonUtils.IGNORE_PROPERTIES, SimpleBeanPropertyFilter.serializeAllExcept(ignores));
    }
}
