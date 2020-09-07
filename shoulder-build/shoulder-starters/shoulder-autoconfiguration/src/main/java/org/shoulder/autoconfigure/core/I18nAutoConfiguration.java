package org.shoulder.autoconfigure.core;

import org.shoulder.core.context.ApplicationInfo;
import org.shoulder.core.i18.ShoulderMessageSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.context.MessageSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractApplicationContext;

import java.time.Duration;

/**
 * 多语言相关配置
 *
 * @author lym
 */
@Configuration(
    proxyBeanMethods = false
)
public class I18nAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MessageSourceProperties.class)
    @ConfigurationProperties(prefix = "spring.messages")
    public MessageSourceProperties messageSourceProperties() {
        return new MessageSourceProperties();
    }

    /**
     * 默认注入 messageSource，其中配置项的 encoding 配置无效，而是使用全局的编码格式
     *
     * @param properties 配置
     * @return messageSource
     */
    @Bean(name = AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME)
    public ShoulderMessageSource shoulderMessageSource(MessageSourceProperties properties) {
        ShoulderMessageSource messageSource = new ShoulderMessageSource();
        messageSource.setDefaultEncoding(ApplicationInfo.charset().name());
        messageSource.setDefaultLocale(ApplicationInfo.defaultLocale());
        messageSource.addBasenames(properties.getBasename());
        messageSource.setFallbackToSystemLocale(properties.isFallbackToSystemLocale());
        messageSource.setUseCodeAsDefaultMessage(properties.isUseCodeAsDefaultMessage());
        messageSource.setAlwaysUseMessageFormat(properties.isAlwaysUseMessageFormat());

        // -1 means never reload cache (spring default)
        long cacheMillis = -1;
        Duration cacheDuration = properties.getCacheDuration();
        if (cacheDuration != null) {
            cacheMillis = cacheDuration.toMillis();
        }
        messageSource.setCacheMillis(cacheMillis);
        return messageSource;
    }

}
