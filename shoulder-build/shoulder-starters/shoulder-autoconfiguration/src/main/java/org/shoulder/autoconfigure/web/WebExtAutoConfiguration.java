package org.shoulder.autoconfigure.web;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.data.mybatis.template.service.BaseServiceImpl;
import org.shoulder.web.template.dictionary.*;
import org.shoulder.web.template.dictionary.repository.DictionaryItemRepository;
import org.shoulder.web.template.dictionary.repository.DictionaryRepository;
import org.shoulder.web.template.dictionary.service.DictionaryItemService;
import org.shoulder.web.template.dictionary.service.DictionaryService;
import org.shoulder.web.template.dictionary.spi.DefaultDictionaryEnumStore;
import org.shoulder.web.template.dictionary.spi.DictionaryEnumStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 自动装配 字典 api，用于动态下拉框
 *
 * @author lym
 */
@ConditionalOnClass(DictionaryController.class)
@Configuration(proxyBeanMethods = false)
public class WebExtAutoConfiguration {

    @Configuration
    @ConditionalOnClass(value = {DictionaryEnumStore.class})
    @EnableConfigurationProperties(WebExProperties.class)
    @ConditionalOnProperty(value = "web.ext.dictionary.storageType", havingValue = "enum", matchIfMissing = true)
    public static class BaseOnEnumDictionaryConfiguration {
        /**
         * 将 String 类型入参，转为 LocalDate 类型
         *
         * @return LocalDateTimeConverter
         */
        @Bean
        @ConditionalOnMissingBean
        public DictionaryEnumStore dictionaryEnumRepository(@Nullable List<DictionaryEnumRepositoryCustomizer> customizers, WebExProperties webExProperties) {
            DictionaryEnumStore repository =
                    new DefaultDictionaryEnumStore(webExProperties.getDictionary().getIgnoreDictionaryTypeCase());
            if (CollectionUtils.isNotEmpty(customizers)) {
                customizers.forEach(c -> c.customize(repository));
            }
            return repository;
        }

        @Bean
        @ConditionalOnMissingBean(value = DictionaryController.class)
        public DictionaryEnumController dictionaryController(DictionaryEnumStore dictionaryEnumStore) {
            return new DictionaryEnumController(dictionaryEnumStore);
        }

        @Bean
        @ConditionalOnMissingBean(value = DictionaryItemController.class)
        public DictionaryItemEnumController dictionaryItemController(DictionaryEnumStore dictionaryEnumStore) {
            return new DictionaryItemEnumController(dictionaryEnumStore);
        }
    }

    @Configuration
    @ConditionalOnClass(value = {BaseServiceImpl.class})
    @ConditionalOnProperty(value = "web.ext.dictionary.storageType", havingValue = "db")
    public static class BaseOnDbDictionaryConfiguration {

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(DictionaryItemRepository.class)
        public DictionaryItemService dictionaryItemService() {
            return new DictionaryItemService();
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(DictionaryRepository.class)
        public DictionaryService dictionaryService() {
            return new DictionaryService();
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(DictionaryService.class)
        public DictionaryController dictionaryController() {
            return new DictionaryCrudController();
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(DictionaryItemService.class)
        public DictionaryItemController dictionaryItemEnumController() {
            return new DictionaryItemCrudController();
        }


    }
}
