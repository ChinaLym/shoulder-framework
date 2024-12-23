package org.shoulder.autoconfigure.web;

import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.web.advice.RestControllerColorfulLogAspect;
import org.shoulder.web.advice.RestControllerDataExceptionAdvice;
import org.shoulder.web.advice.RestControllerExceptionAdvice;
import org.shoulder.web.advice.RestControllerJsonLogAspect;
import org.shoulder.web.advice.RestControllerUnionResponseAdvice;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;

import java.util.List;

/**
 * web 增强切面相关配置
 *
 * @author lym
 */
@ConditionalOnClass(value = SkipResponseWrap.class)
@AutoConfiguration
@ConditionalOnWebApplication
@EnableConfigurationProperties(WebProperties.class)
public class WebAdvanceAutoConfiguration {


    /**
     * RestController 全局异常处理器
     * 默认情况下该类优先用户自定义的全局处理器，如果使用者指定 @Order且小于0，则优先于本框架处理
     */
    @Bean
    @Order(value = 0)
    @ConditionalOnProperty(name = "shoulder.web.handleGlobalException", havingValue = "true", matchIfMissing = true)
    public RestControllerExceptionAdvice restControllerExceptionAdvice() {
        return new RestControllerExceptionAdvice();
    }

    @AutoConfiguration
    @ConditionalOnClass(DataAccessException.class)
    @ConditionalOnProperty(name = "shoulder.web.handleGlobalException", havingValue = "true", matchIfMissing = true)
    public static class RestControllerDataExceptionAdviceAutoConfiguration {

        /**
         * RestController 数据库异常处理
         */
        @Bean
        @Order(value = 1)
        public RestControllerDataExceptionAdvice restControllerDataExceptionAdvice() {
            return new RestControllerDataExceptionAdvice();
        }
    }


    /**
     * RestController 全局异常处理器
     * 默认情况下该类优先用户自定义的全局处理器，如果使用者指定 @Order且小于0，则优先于本框架处理
     */
    @Bean
    @Order(value = 0)
    @ConditionalOnProperty(name = "shoulder.web.restResponse.autoWrapFormat", havingValue = "true", matchIfMissing = true)
    public RestControllerUnionResponseAdvice restControllerUnionResponseAdvice(
            @Value("#{'${shoulder.web.restResponse.skipWrapPathPatterns:}'.split(',')}") List<String> skipWarpPathPatterns) {
        return new RestControllerUnionResponseAdvice(skipWarpPathPatterns);
    }

    /**
     * RestController 接口调用汇报器（打印调用日志）【用于开发态】日志打在多行，且带颜色，代码跳转
     * 默认情况下该类优先用户自定义的全局处理器，如果使用者指定 @Order且小于0，则优先于本框架处理
     */
    @Bean
    @Order(value = 0)
    @ConditionalOnProperty(name = "shoulder.web.log.type", havingValue = "colorful", matchIfMissing = true)
    public RestControllerColorfulLogAspect restControllerColorfulLogAspect(
        @Value("${shoulder.web.log.combineReqResp:true}") boolean logTillResponse,
        @Value("${shoulder.web.log.useCallerLogger:true}") boolean useCallerLogger) {
        ShoulderLoggers.SHOULDER_CONFIG.info("active shoulder.web.log.type=colorful");
        return new RestControllerColorfulLogAspect(useCallerLogger, logTillResponse);
    }

    /**
     * RestController 接口调用汇报器（打印调用日志）【用于生产态】日志打在一行中
     * 默认情况下该类优先用户自定义的全局处理器，如果使用者指定 @Order且小于0，则优先于本框架处理
     */
    @Bean
    @Order(value = 0)
    @ConditionalOnProperty(name = "shoulder.web.log.type", havingValue = "json")
    public RestControllerJsonLogAspect restControllerJsonLogAspect(
        @Value("${shoulder.web.log.useCallerLogger:false}") boolean useCallerLogger
    ) {
        ShoulderLoggers.SHOULDER_CONFIG.info("active shoulder.web.log.type=json");
        return new RestControllerJsonLogAspect(useCallerLogger);
    }

}
