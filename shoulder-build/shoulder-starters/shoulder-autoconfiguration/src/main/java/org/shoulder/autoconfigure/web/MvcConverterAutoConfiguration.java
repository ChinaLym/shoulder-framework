package org.shoulder.autoconfigure.web;

import org.shoulder.core.converter.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MvcConverterAutoConfiguration
 *
 * @author lym
 */
@Configuration
@ConditionalOnWebApplication
public class MvcConverterAutoConfiguration {


    /**
     * 将 String 类型入参，转为 Date 类型
     * @return DateConverter
     */
    @Bean
    public DateConverter dateConverter(){
        return new DateConverter();
    }

    /**
     * 将 String 类型入参，转为 LocalDate 类型
     * @return LocalDateConverter
     */
    @Bean
    public LocalDateConverter localDateConverter(){
        return new LocalDateConverter();
    }

    /**
     * 将 String 类型入参，转为 LocalDate 类型
     * @return LocalDateTimeConverter
     */
    @Bean
    public LocalDateTimeConverter localDateTimeConverter(){
        return new LocalDateTimeConverter();
    }

    /**
     * 将 String 类型入参，转为 LocalTime 类型
     * @return LocalTimeConverter
     */
    @Bean
    public LocalTimeConverter localTimeConverter(){
        return new LocalTimeConverter();
    }

    /**
     * 默认的枚举转换失败处理器，若转换失败则返回 null
     * @return EnumMissMatchHandler
     */
    @Bean
    public EnumMissMatchHandler enumMissMatchHandler(){
        return new DefaultEnumMissMatchHandler();
    }

}
