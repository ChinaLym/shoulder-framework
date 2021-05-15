package org.shoulder.autoconfigure.web;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.web.template.dictionary.DefaultDictionaryController;
import org.shoulder.web.template.dictionary.DefaultDictionaryItemController;
import org.shoulder.web.template.dictionary.DictionaryController;
import org.shoulder.web.template.dictionary.DictionaryItemController;
import org.shoulder.web.template.dictionary.spi.DefaultDictionaryEnumStore;
import org.shoulder.web.template.dictionary.spi.DictionaryEnumStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 自动装配
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WebExProperties.class)
public class WebExtAutoConfiguration {

    /**
     * config
     */
    private final WebExProperties webExProperties;

    public WebExtAutoConfiguration(WebExProperties webExProperties) {
        this.webExProperties = webExProperties;
    }

    /**
     * 将 String 类型入参，转为 LocalDate 类型
     *
     * @return LocalDateTimeConverter
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "web.ext.dictionary.storageType", havingValue = "enum", matchIfMissing = true)
    public DictionaryEnumStore dictionaryEnumRepository(@Nullable List<DictionaryEnumRepositoryCustomizer> customizers) {
        DictionaryEnumStore repository =
                new DefaultDictionaryEnumStore(webExProperties.getDictionary().getIgnoreDictionaryTypeCase());
        if (CollectionUtils.isNotEmpty(customizers)) {
            customizers.forEach(c -> c.customize(repository));
        }
        return repository;
    }

    /**
     * 枚举字典 api，用于简单动态下拉框
     *
     * @return DefaultDictionaryController
     */
    @Bean
    @ConditionalOnMissingBean
    public DictionaryController dictionaryController() {
        return new DefaultDictionaryController();
    }

    @Bean
    @ConditionalOnMissingBean
    public DictionaryItemController dictionaryItemEnumController(@Autowired(required = false) DictionaryEnumStore dictionaryEnumStore) {
        return new DefaultDictionaryItemController(dictionaryEnumStore);
    }


}
