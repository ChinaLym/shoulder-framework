package org.shoulder.autoconfigure.web;

import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.shoulder.autoconfigure.core.CacheAutoConfiguration;
import org.shoulder.autoconfigure.core.I18nAutoConfiguration;
import org.shoulder.autoconfigure.web.WebExtAutoConfiguration.BaseOnEnumDictionaryConfiguration;
import org.shoulder.core.converter.ShoulderConversionService;
import org.shoulder.core.i18.Translator;
import org.shoulder.web.template.dictionary.controller.DictionaryController;
import org.shoulder.web.template.dictionary.controller.DictionaryEnumController;
import org.shoulder.web.template.dictionary.controller.DictionaryItemController;
import org.shoulder.web.template.dictionary.controller.DictionaryItemEnumController;
import org.shoulder.web.template.dictionary.convert.DictionaryItemDTO2DomainConverterRegister;
import org.shoulder.web.template.dictionary.convert.DictionaryItemDomain2DTOConverter;
import org.shoulder.web.template.dictionary.spi.DefaultDictionaryEnumStore;
import org.shoulder.web.template.dictionary.spi.DictionaryEnumStore;
import org.shoulder.web.template.tag.controller.TagController;
import org.shoulder.web.template.tag.controller.TagCrudController;
import org.shoulder.web.template.tag.converter.TagDTO2DomainConverter;
import org.shoulder.web.template.tag.converter.TagDomain2DTOConverter;
import org.shoulder.web.template.tag.repository.TagMappingService;
import org.shoulder.web.template.tag.repository.TagRepository;
import org.shoulder.web.template.tag.service.TagCoreService;
import org.shoulder.web.template.tag.service.TagServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.EnableWebMvcConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

import java.util.List;

/**
 * 自动装配 字典 api，用于动态下拉框
 *
 * @author lym
 */
@ConditionalOnClass(DictionaryController.class)
@AutoConfiguration(before = { EnableWebMvcConfiguration.class }, after = { BaseOnEnumDictionaryConfiguration.class })
public class WebExtAutoConfiguration {

    @MapperScan("org.shoulder.web.template.tag.mapper")
    @AutoConfiguration(after = {I18nAutoConfiguration.class, CacheAutoConfiguration.class})
    @ConditionalOnClass(value = { TagController .class })
    @EnableConfigurationProperties(WebExProperties.class)
    @ConditionalOnProperty(value = "shoulder.web.ext.tag.enable", havingValue = "true", matchIfMissing = false)
    public static class ExtTagAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean(value = TagDomain2DTOConverter.class)
        public TagDomain2DTOConverter tagDomain2DTOConverter() {
            return new TagDomain2DTOConverter();
        }

        @Bean
        @ConditionalOnMissingBean(value = TagDTO2DomainConverter .class)
        public TagDTO2DomainConverter  tagDTO2DomainConverter() {
            return new TagDTO2DomainConverter ();
        }

        @Bean
        @ConditionalOnMissingBean(value = TagMappingService.class)
        public TagMappingService tagMappingService() {
            return new TagMappingService();
        }

        @Bean
        @ConditionalOnMissingBean(value = TagRepository.class)
        public TagRepository tagRepository() {
            return new TagRepository();
        }
        @Bean
        @ConditionalOnMissingBean(value = TagCoreService.class)
        public TagServiceImpl tagCoreService() {
            return new TagServiceImpl();
        }

        @Bean
        @DependsOn({"tagCoreService", "conversionService"})
        @ConditionalOnMissingBean(value = TagController.class)
        public TagCrudController tagCrudController() {
            return new TagCrudController();
        }

    }

    @AutoConfiguration(after = { I18nAutoConfiguration.class })
    @ConditionalOnClass(value = { DictionaryEnumStore.class })
    @EnableConfigurationProperties(WebExProperties.class)
    @ConditionalOnProperty(value = "shoulder.web.ext.dictionary.storageType", havingValue = "enum", matchIfMissing = true)
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
    @ConditionalOnProperty(value = "shoulder.web.ext.dictionary.storageType", havingValue = "db")
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
