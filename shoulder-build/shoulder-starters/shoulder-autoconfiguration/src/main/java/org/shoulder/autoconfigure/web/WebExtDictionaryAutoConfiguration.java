package org.shoulder.autoconfigure.web;

import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.shoulder.autoconfigure.core.CacheAutoConfiguration;
import org.shoulder.autoconfigure.core.I18nAutoConfiguration;
import org.shoulder.core.converter.ShoulderConversionService;
import org.shoulder.core.dictionary.spi.DefaultDictionaryEnumStore;
import org.shoulder.core.dictionary.spi.DictionaryEnumStore;
import org.shoulder.core.i18.Translator;
import org.shoulder.data.mybatis.template.service.BaseServiceImpl;
import org.shoulder.web.template.dictionary.controller.DictionaryEnumController;
import org.shoulder.web.template.dictionary.controller.DictionaryEnumQueryController;
import org.shoulder.web.template.dictionary.controller.DictionaryItemController;
import org.shoulder.web.template.dictionary.controller.DictionaryItemCrudController;
import org.shoulder.web.template.dictionary.controller.DictionaryItemEnumController;
import org.shoulder.web.template.dictionary.controller.DictionaryTypeController;
import org.shoulder.web.template.dictionary.controller.DictionaryTypeCrudController;
import org.shoulder.web.template.dictionary.convert.DictionaryItemDTO2DomainConverterRegister;
import org.shoulder.web.template.dictionary.convert.DictionaryItemDomain2DTOConverter;
import org.shoulder.web.template.dictionary.convert.DictionaryTypeDTO2EntityConverter;
import org.shoulder.web.template.dictionary.convert.DictionaryTypeDomain2DTOConverter;
import org.shoulder.web.template.dictionary.convert.DictionaryTypeEntity2DTOConverter;
import org.shoulder.web.template.dictionary.service.DictionaryItemService;
import org.shoulder.web.template.dictionary.service.DictionaryService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.EnableWebMvcConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 自动装配 字典 api，用于动态下拉框
 *
 * @author lym
 */
@ConditionalOnClass(DictionaryEnumQueryController.class)
@AutoConfiguration(before = {EnableWebMvcConfiguration.class})
@EnableConfigurationProperties(WebExtProperties.class)
@ConditionalOnProperty(value = "shoulder.web.ext.dictionary.enable", havingValue = "true", matchIfMissing = true)
public class WebExtDictionaryAutoConfiguration {

    @AutoConfiguration(after = {I18nAutoConfiguration.class})
    @ConditionalOnClass(value = {DictionaryEnumStore.class})
    @ConditionalOnProperty(value = "shoulder.web.ext.dictionary.storage", havingValue = "ENUM", matchIfMissing = true)
    public static class BaseOnEnumDictionaryConfiguration {

        /**
         * 将 String 类型入参，转为 LocalDate 类型
         *
         * @return LocalDateTimeConverter
         */
        @Bean
        @ConditionalOnMissingBean
        public DictionaryEnumStore dictionaryEnumRepository(@Nullable List<DictionaryEnumRepositoryCustomizer> customizers,
                                                            WebExtProperties webExtProperties) {
            DictionaryEnumStore repository =
                    new DefaultDictionaryEnumStore(webExtProperties.getDictionary().getIgnoreDictionaryTypeCase());
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
        @ConditionalOnMissingBean(value = DictionaryItemDomain2DTOConverter.class)
        public DictionaryItemDomain2DTOConverter dictionaryItemDomain2DTOConverter(Translator translator) {
            DictionaryItemDomain2DTOConverter.INSTANCE = new DictionaryItemDomain2DTOConverter(translator);
            return DictionaryItemDomain2DTOConverter.INSTANCE;
        }

        @Bean
        @ConditionalOnMissingBean(value = DictionaryEnumQueryController.class)
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

    @MapperScan("org.shoulder.web.template.dictionary.mapper")
    @AutoConfiguration(after = {I18nAutoConfiguration.class, CacheAutoConfiguration.class})
    @ConditionalOnClass(value = {BaseServiceImpl.class, DictionaryTypeCrudController.class})
    @ConditionalOnProperty(value = "shoulder.web.ext.dictionary.storage", havingValue = "DB")
    public static class BaseOnDbDictionaryConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public DictionaryService dictionaryService() {
            return new DictionaryService();
        }

        @Bean
        @ConditionalOnMissingBean(DictionaryTypeController.class)
        public DictionaryTypeCrudController dictionaryCrudController(DictionaryService service, ShoulderConversionService conversionService) {
            return new DictionaryTypeCrudController(service, conversionService);
        }

        @Bean
        @ConditionalOnMissingBean
        public DictionaryItemService dictionaryItemService() {
            return new DictionaryItemService();
        }

        @Bean
        @ConditionalOnMissingBean(DictionaryItemController.class)
        public DictionaryItemCrudController dictionaryItemCrudController(DictionaryItemService dictionaryItemService,
                                                                         ShoulderConversionService conversionService) {
            return new DictionaryItemCrudController(dictionaryItemService, conversionService);
        }

        @Bean
        @ConditionalOnMissingBean(value = DictionaryTypeDTO2EntityConverter.class)
        public DictionaryTypeDTO2EntityConverter dictionaryTypeDTO2EntityConverter() {
            return new DictionaryTypeDTO2EntityConverter();
        }

        @Bean
        @ConditionalOnMissingBean(value = DictionaryTypeEntity2DTOConverter.class)
        public DictionaryTypeEntity2DTOConverter dictionaryTypeEntity2DTOConverter() {
            return new DictionaryTypeEntity2DTOConverter();
        }

        @Bean
        @ConditionalOnMissingBean(value = DictionaryTypeDomain2DTOConverter.class)
        public DictionaryTypeDomain2DTOConverter dictionaryTypeDomain2DTOConverter() {
            return new DictionaryTypeDomain2DTOConverter();
        }

    }
}
