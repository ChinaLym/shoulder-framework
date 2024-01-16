package org.shoulder.autoconfigure.web;

import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.autoconfigure.core.I18nAutoConfiguration;
import org.shoulder.autoconfigure.web.WebExtAutoConfiguration.BaseOnEnumDictionaryConfiguration;
import org.shoulder.core.converter.ShoulderConversionService;
import org.shoulder.core.converter.ShoulderGenericConversionServiceImpl;
import org.shoulder.core.i18.Translator;
import org.shoulder.core.util.ConvertUtil;
import org.shoulder.web.template.dictionary.DictionaryController;
import org.shoulder.web.template.dictionary.DictionaryEnumController;
import org.shoulder.web.template.dictionary.DictionaryItemController;
import org.shoulder.web.template.dictionary.DictionaryItemEnumController;
import org.shoulder.web.template.dictionary.base.DictionaryItemDTO2DomainConverterRegister;
import org.shoulder.web.template.dictionary.base.ToDictionaryEnumConversionService;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDomain2DTOConverter;
import org.shoulder.web.template.dictionary.spi.DefaultDictionaryEnumStore;
import org.shoulder.web.template.dictionary.spi.DictionaryEnumStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.EnableWebMvcConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;

import java.util.List;
import java.util.Optional;

/**
 * 自动装配 字典 api，用于动态下拉框
 *
 * @author lym
 */
@ConditionalOnClass(DictionaryController.class)
@AutoConfiguration(before = { EnableWebMvcConfiguration.class }, after = { BaseOnEnumDictionaryConfiguration.class })
public class WebExtAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ShoulderConversionService shoulderConversionService(@Nullable List<ConversionService> conversionServiceList) {
        DateTimeFormatters dateTimeFormatters = new DateTimeFormatters()
            .dateFormat(ConvertUtil.ISO_DATE_FORMAT)
            .timeFormat(ConvertUtil.ISO_DATE_FORMAT)
            .dateTimeFormat(ConvertUtil.ISO_DATE_FORMAT);
        ShoulderGenericConversionServiceImpl conversionService = new ShoulderGenericConversionServiceImpl(dateTimeFormatters);
        Optional.ofNullable(conversionServiceList).ifPresent(l -> l.forEach(conversionService::addConversionService));
        ConvertUtil.setConversionService(conversionService);
        return conversionService;
    }

    @AutoConfiguration(after = { I18nAutoConfiguration.class })
    @ConditionalOnClass(value = { DictionaryEnumStore.class })
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
        public DictionaryEnumStore dictionaryEnumRepository(@Nullable List<DictionaryEnumRepositoryCustomizer> customizers,
                                                            WebExProperties webExProperties) {
            DictionaryEnumStore repository =
                new DefaultDictionaryEnumStore(webExProperties.getDictionary().getIgnoreDictionaryTypeCase());
            if (CollectionUtils.isNotEmpty(customizers)) {
                customizers.forEach(c -> c.customize(repository));
            }
            return repository;
        }

        @Bean
        @ConditionalOnMissingBean
        public ToDictionaryEnumConversionService dictionaryConversionService(DictionaryEnumStore dictionaryEnumStore) {
            return new ToDictionaryEnumConversionService(dictionaryEnumStore);
        }

        @Bean
        @ConditionalOnMissingBean(value = DictionaryItemDTO2DomainConverterRegister.class)
        public DictionaryItemDTO2DomainConverterRegister dictionaryItemDTO2DomainConverterRegister() {
            return new DictionaryItemDTO2DomainConverterRegister();
        }

        @Bean
        @ConditionalOnMissingBean(value = DictionaryItemDomain2DTOConverter.class)
        public DictionaryItemDomain2DTOConverter dictionaryItemDomain2DTOConverter(Translator translator) {
            return new DictionaryItemDomain2DTOConverter(translator);
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
