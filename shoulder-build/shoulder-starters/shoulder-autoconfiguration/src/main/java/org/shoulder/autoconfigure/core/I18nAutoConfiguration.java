package org.shoulder.autoconfigure.core;

import org.shoulder.core.context.AppInfo;
import org.shoulder.core.i18.ReloadableLocaleDirectoryMessageSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.context.MessageSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.AbstractApplicationContext;

import java.time.Duration;

/**
 * 多语言相关配置
 *
 * @author lym
 */
@AutoConfiguration
public class I18nAutoConfiguration {

    public I18nAutoConfiguration() {
        // just for debug
    }

    @Bean
    @ConditionalOnMissingBean(MessageSourceProperties.class)
    @ConfigurationProperties(prefix = "spring.messages")
    public MessageSourceProperties messageSourceProperties() {
        // 修改默认多语言资源路径
        MessageSourceProperties messageSourceProperties = new MessageSourceProperties();
        messageSourceProperties.setBasename("classpath*:language");
        return messageSourceProperties;
    }

    /**
     * 默认注入 messageSource，其中配置项的 encoding 配置无效，而是使用全局的编码格式
     *
     * @param properties 配置
     * @return messageSource
     */
    @Bean(name = AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME)
    @ConditionalOnMissingBean
    public ReloadableLocaleDirectoryMessageSource reloadableLocaleDirectoryMessageSource(MessageSourceProperties properties) {
        ReloadableLocaleDirectoryMessageSource messageSource = new ReloadableLocaleDirectoryMessageSource();
        messageSource.setDefaultEncoding(AppInfo.charset().name());
        messageSource.setDefaultLocale(AppInfo.defaultLocale());
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
