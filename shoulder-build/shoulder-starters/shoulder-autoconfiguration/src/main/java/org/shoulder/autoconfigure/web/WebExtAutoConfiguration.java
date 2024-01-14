package org.shoulder.autoconfigure.web;

import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.web.template.dictionary.DictionaryController;
import org.shoulder.web.template.dictionary.DictionaryEnumController;
import org.shoulder.web.template.dictionary.DictionaryItemController;
import org.shoulder.web.template.dictionary.DictionaryItemEnumController;
import org.shoulder.web.template.dictionary.base.DictionaryItemDTO2DomainConverterRegister;
import org.shoulder.web.template.dictionary.base.ShoulderConversionService;
import org.shoulder.web.template.dictionary.base.ShoulderGenericConversionServiceImpl;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDomain2ConverDTOter;
import org.shoulder.web.template.dictionary.spi.DefaultDictionaryEnumStore;
import org.shoulder.web.template.dictionary.spi.DictionaryEnumStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 自动装配 字典 api，用于动态下拉框
 *
 * @author lym
 */
@ConditionalOnClass(DictionaryController.class)
@AutoConfiguration
public class WebExtAutoConfiguration {


    @AutoConfiguration
    @ConditionalOnClass(value = {DictionaryEnumStore.class})
    @EnableConfigurationProperties(WebExProperties.class)
    @ConditionalOnProperty(value = "web.ext.dictionary.storageType", havingValue = "enum", matchIfMissing = true)
    public static class BaseOnEnumDictionaryConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ShoulderConversionService shoulderConversionService(DictionaryEnumStore dictionaryEnumStore) {
            return new ShoulderGenericConversionServiceImpl(dictionaryEnumStore);
        }

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
        @ConditionalOnMissingBean(value = DictionaryItemDTO2DomainConverterRegister.class)
        public DictionaryItemDTO2DomainConverterRegister dictionaryItemDTO2DomainConverterRegister() {
            return new DictionaryItemDTO2DomainConverterRegister();
        }

        @Bean
        @ConditionalOnMissingBean(value = DictionaryItemDomain2ConverDTOter.class)
        public DictionaryItemDomain2ConverDTOter dictionaryItemDomain2DTOConverter() {
            return new DictionaryItemDomain2ConverDTOter();
        }

        @Bean
        @ConditionalOnMissingBean(value = DictionaryController.class)
        public DictionaryEnumController dictionaryController(DictionaryEnumStore dictionaryEnumStore) {
            return new DictionaryEnumController(dictionaryEnumStore);
        }

        @Bean
        @ConditionalOnMissingBean(value = DictionaryItemController.class)
        public DictionaryItemEnumController dictionaryItemController(DictionaryEnumStore dictionaryEnumStore,
                                                                     ShoulderConversionService conversionService) {
            return new DictionaryItemEnumController(dictionaryEnumStore, conversionService);
        }
    }

    /*@Configuration
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


    }*/
}
