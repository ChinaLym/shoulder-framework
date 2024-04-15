package org.shoulder.autoconfigure.web;

import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.shoulder.autoconfigure.core.CacheAutoConfiguration;
import org.shoulder.autoconfigure.core.I18nAutoConfiguration;
import org.shoulder.autoconfigure.web.WebExtAutoConfiguration.BaseOnEnumDictionaryConfiguration;
import org.shoulder.core.converter.ShoulderConversionService;
import org.shoulder.core.dictionary.spi.DefaultDictionaryEnumStore;
import org.shoulder.core.dictionary.spi.DictionaryEnumStore;
import org.shoulder.core.i18.Translator;
import org.shoulder.data.mybatis.template.service.BaseServiceImpl;
import org.shoulder.web.template.dictionary.controller.*;
import org.shoulder.web.template.dictionary.convert.*;
import org.shoulder.web.template.dictionary.service.DictionaryItemService;
import org.shoulder.web.template.dictionary.service.DictionaryService;
import org.shoulder.web.template.oplog.controller.OperationLogQueryController;
import org.shoulder.web.template.oplog.convert.OperationLogDTO2EntityConverter;
import org.shoulder.web.template.oplog.convert.OperationLogEntity2DTOConverter;
import org.shoulder.web.template.oplog.service.OperationLogService;
import org.shoulder.web.template.tag.controller.TagController;
import org.shoulder.web.template.tag.controller.TagCrudController;
import org.shoulder.web.template.tag.converter.TagDTO2DomainConverter;
import org.shoulder.web.template.tag.converter.TagDomain2DTOConverter;
import org.shoulder.web.template.tag.service.TagCoreService;
import org.shoulder.web.template.tag.service.TagMappingService;
import org.shoulder.web.template.tag.service.TagServiceImpl;
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
@AutoConfiguration(before = { EnableWebMvcConfiguration.class }, after = { BaseOnEnumDictionaryConfiguration.class })
public class WebExtAutoConfiguration {

    @MapperScan("org.shoulder.web.template.tag.mapper")
    @AutoConfiguration(after = { I18nAutoConfiguration.class, CacheAutoConfiguration.class })
    @ConditionalOnClass(value = { BaseServiceImpl.class, TagController.class })
    @EnableConfigurationProperties(WebExtProperties.class)
    @ConditionalOnProperty(value = "shoulder.web.ext.tag.enable", havingValue = "true", matchIfMissing = false)
    public static class ExtTagAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean(value = TagDomain2DTOConverter.class)
        public TagDomain2DTOConverter tagDomain2DTOConverter() {
            return new TagDomain2DTOConverter();
        }

        @Bean
        @ConditionalOnMissingBean(value = TagDTO2DomainConverter.class)
        public TagDTO2DomainConverter tagDTO2DomainConverter() {
            return new TagDTO2DomainConverter();
        }

        @Bean
        @ConditionalOnMissingBean(value = TagMappingService.class)
        public TagMappingService tagMappingService() {
            return new TagMappingService();
        }

        @Bean
        @ConditionalOnMissingBean(value = TagCoreService.class)
        public TagServiceImpl tagCoreService() {
            return new TagServiceImpl();
        }

        /**
         * 想要 autoconfiguration 注入的 Controller 生效（能处理请求）
         * 必须要返回值是具体的 Controller，而非接口；另外注意 ConditionalOnMissingBean 要用接口，方便使用者替换
         */
        @Bean
        @ConditionalOnMissingBean(value = TagController.class)
        public TagCrudController tagCrudController(TagServiceImpl service, ShoulderConversionService conversionService, TagCoreService tagCoreService) {
            return new TagCrudController(service, conversionService, tagCoreService);
        }

    }


    @MapperScan("org.shoulder.web.template.oplog.mapper")
    @AutoConfiguration(after = {I18nAutoConfiguration.class, CacheAutoConfiguration.class})
    @ConditionalOnClass(value = {BaseServiceImpl.class, OperationLogQueryController.class})
    @EnableConfigurationProperties(WebExtProperties.class)
    @ConditionalOnProperty(value = "shoulder.web.ext.oplog.enable", havingValue = "true", matchIfMissing = false)
    public static class ExtOpLogAutoConfiguration {

        @Bean("operationLogService")
        @ConditionalOnMissingBean(value = OperationLogService.class)
        public OperationLogService operationLogService() {
            return new OperationLogService();
        }

        /**
         * 想要 autoconfiguration 注入的 Controller 生效（能处理请求）
         * 必须要返回值是具体的 Controller，而非接口；另外注意 ConditionalOnMissingBean 要用接口，方便使用者替换
         */
        @Bean
        public OperationLogQueryController operationLogQueryController(OperationLogService service, ShoulderConversionService conversionService) {
            return new OperationLogQueryController(service, conversionService);
        }

        @Bean
        @ConditionalOnMissingBean(value = OperationLogDTO2EntityConverter.class)
        public OperationLogDTO2EntityConverter operationLogDTO2EntityConverter() {
            return new OperationLogDTO2EntityConverter();
        }

        @Bean
        @ConditionalOnMissingBean(value = OperationLogEntity2DTOConverter.class)
        public OperationLogEntity2DTOConverter operationLogEntity2DTOConverter() {
            return new OperationLogEntity2DTOConverter();
        }

    }

    @AutoConfiguration(after = { I18nAutoConfiguration.class })
    @ConditionalOnClass(value = { DictionaryEnumStore.class })
    @EnableConfigurationProperties(WebExtProperties.class)
    @ConditionalOnProperty(value = "shoulder.web.ext.dictionary.enableEnum", havingValue = "true", matchIfMissing = true)
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
    @AutoConfiguration(after = { I18nAutoConfiguration.class, CacheAutoConfiguration.class })
    @EnableConfigurationProperties(WebExtProperties.class)
    @ConditionalOnClass(value = {BaseServiceImpl.class, DictionaryTypeCrudController.class})
    @ConditionalOnProperty(value = "shoulder.web.ext.dictionary.enable", havingValue = "true", matchIfMissing = false)
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
        public DictionaryTypeDTO2EntityConverter  dictionaryTypeDTO2EntityConverter() {
            return new DictionaryTypeDTO2EntityConverter();
        }

        @Bean
        @ConditionalOnMissingBean(value = DictionaryTypeEntity2DTOConverter.class)
        public DictionaryTypeEntity2DTOConverter dictionaryTypeEntity2DTOConverter() {
            return new DictionaryTypeEntity2DTOConverter();
        }

        @Bean
        @ConditionalOnMissingBean(value = DictionaryTypeDomain2DTOConverter.class)
        public DictionaryTypeDomain2DTOConverter  dictionaryTypeDomain2DTOConverter() {
            return new DictionaryTypeDomain2DTOConverter();
        }

    }
}
