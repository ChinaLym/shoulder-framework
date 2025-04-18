package org.shoulder.autoconfigure.web;

import org.mybatis.spring.annotation.MapperScan;
import org.shoulder.autoconfigure.core.CacheAutoConfiguration;
import org.shoulder.autoconfigure.core.I18nAutoConfiguration;
import org.shoulder.core.converter.ShoulderConversionService;
import org.shoulder.data.mybatis.template.service.BaseServiceImpl;
import org.shoulder.web.template.dictionary.controller.DictionaryEnumQueryController;
import org.shoulder.web.template.oplog.controller.OperationLogPageController;
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

/**
 * 自动装配 字典 api，用于动态下拉框
 *
 * @author lym
 */
@ConditionalOnClass(DictionaryEnumQueryController.class)
@AutoConfiguration(before = {EnableWebMvcConfiguration.class}, after = {WebExtDictionaryAutoConfiguration.BaseOnEnumDictionaryConfiguration.class})
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
        public OperationLogPageController operationLogPageController() {
            return new OperationLogPageController();
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

}
