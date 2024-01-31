package org.shoulder.autoconfigure.web;

import org.apache.commons.lang3.time.FastDateFormat;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.converter.DateConverter;
import org.shoulder.core.converter.DefaultEnumMissMatchHandler;
import org.shoulder.core.converter.EnumConverterFactory;
import org.shoulder.core.converter.EnumMissMatchHandler;
import org.shoulder.core.converter.LocalDateConverter;
import org.shoulder.core.converter.LocalDateTimeConverter;
import org.shoulder.core.converter.LocalTimeConverter;
import org.shoulder.core.converter.ShoulderConversionService;
import org.shoulder.core.converter.ShoulderConversionServiceImpl;
import org.shoulder.core.util.ConvertUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.util.Map;

/**
 * ConverterAutoConfiguration
 *
 * @author lym
 */
@AutoConfiguration
@ConditionalOnWebApplication
public class ShoulderConverterAutoConfiguration implements ApplicationListener<ContextRefreshedEvent> {


    /**
     * 名称为 spring 上下文的默认转化器名称
     * @see org.springframework.context.support.AbstractApplicationContext#finishBeanFactoryInitialization
     */
    @Bean
    @ConditionalOnMissingBean
    public ShoulderConversionService conversionService() {
        DateTimeFormatters dateTimeFormatters = new DateTimeFormatters()
            .dateFormat(ConvertUtil.ISO_DATE_FORMAT)
            .timeFormat(ConvertUtil.ISO_DATE_FORMAT)
            .dateTimeFormat(ConvertUtil.ISO_DATE_FORMAT);
        ShoulderConversionServiceImpl conversionService = new ShoulderConversionServiceImpl(dateTimeFormatters);
        ConvertUtil.setConversionService(conversionService);
        return conversionService;
    }

    /**
     * 将 String 类型入参，转为 Date 类型
     *
     * @return DateConverter
     */
    @Bean
    @ConditionalOnMissingBean
    public DateConverter dateConverter() {
        // 重置日期格式，以配置为准
        DateConverter.FORMATTER = FastDateFormat.getInstance(AppInfo.dateTimeFormat());
        return DateConverter.INSTANCE;
    }

    /**
     * 将 String 类型入参，转为 LocalDate 类型
     *
     * @return LocalDateConverter
     */
    @Bean
    @ConditionalOnMissingBean
    public LocalDateConverter localDateConverter() {
        return LocalDateConverter.INSTANCE;
    }

    /**
     * 将 String 类型入参，转为 LocalDate 类型
     *
     * @return LocalDateTimeConverter
     */
    @Bean
    @ConditionalOnMissingBean
    public LocalDateTimeConverter localDateTimeConverter() {
        return LocalDateTimeConverter.INSTANCE;
    }

    /**
     * 将 String 类型入参，转为 LocalTime 类型
     *
     * @return LocalTimeConverter
     */
    @Bean
    @ConditionalOnMissingBean
    public LocalTimeConverter localTimeConverter() {
        return LocalTimeConverter.INSTANCE;
    }

    /**
     * 字符串参数自动转枚举-默认实现
     *
     * @return EnumConverterFactory
     */
    @Bean
    @ConditionalOnMissingBean
    public EnumConverterFactory enumConverterFactory(EnumMissMatchHandler missMatchHandler) {
        return new EnumConverterFactory(missMatchHandler);
    }

    /**
     * 默认的枚举转换失败处理器，若转换失败则返回 null
     *
     * @return EnumMissMatchHandler
     */
    @Bean
    @ConditionalOnMissingBean
    public EnumMissMatchHandler enumMissMatchHandler() {
        return DefaultEnumMissMatchHandler.getInstance();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ConvertUtil.addConverter(DateConverter.INSTANCE);
        ConvertUtil.addConverter(LocalDateConverter.INSTANCE);
        ConvertUtil.addConverter(LocalTimeConverter.INSTANCE);
        ConvertUtil.addConverter(LocalDateTimeConverter.INSTANCE);
        ConvertUtil.addConverterFactory(EnumConverterFactory.getDefaultInstance());

        ShoulderConversionService shoulderConversionService = event.getApplicationContext().getBean(ShoulderConversionService.class);

        // ShoulderConversionService ext
        Map<String, ConversionService> allConversionServices = event.getApplicationContext().getBeansOfType(ConversionService.class);
        allConversionServices.values().stream()
            .filter(conversionService -> !(conversionService instanceof ShoulderConversionService))
            .forEach(shoulderConversionService::addConversionService);

        // spring converter discover
        Map<String, Converter> allConverter = event.getApplicationContext().getBeansOfType(Converter.class);
        for (Converter converter : allConverter.values()) {
            shoulderConversionService.addConverter(converter);
        }
        Map<String, ConverterFactory> allConverterFactories = event.getApplicationContext().getBeansOfType(ConverterFactory.class);
        for (ConverterFactory converterFactory : allConverterFactories.values()) {
            shoulderConversionService.addConverterFactory(converterFactory);
        }
    }
}
