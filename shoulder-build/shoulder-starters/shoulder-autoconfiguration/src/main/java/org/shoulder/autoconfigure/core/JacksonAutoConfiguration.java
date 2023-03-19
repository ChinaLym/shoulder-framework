package org.shoulder.autoconfigure.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.shoulder.core.util.JsonUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * jackson配置
 *
 * @author lym
 */
@ConditionalOnClass(ObjectMapper.class)
@AutoConfiguration(before = org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class)
public class JacksonAutoConfiguration implements WebMvcConfigurer {

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public ObjectMapper jacksonObjectMapper() {
        //public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        //ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        return JsonUtils.createObjectMapper();
    }
}
