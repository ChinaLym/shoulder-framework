package org.shoulder.autoconfigure.web;

import org.shoulder.web.advice.RestControllerColorfulLogAspect;
import org.shoulder.web.advice.RestControllerExceptionAdvice;
import org.shoulder.web.advice.RestControllerJsonLogAspect;
import org.shoulder.web.advice.RestControllerUnionResponseAdvice;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * web 增强切面相关配置
 *
 * @author lym
 */
@ConditionalOnClass(value = SkipResponseWrap.class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
public class WebAdvanceAutoConfiguration {


    /**
     * RestController 全局异常处理器
     * 默认情况下该类优先用户自定义的全局处理器，如果使用者指定 @Order且小于0，则优先于本框架处理
     */
    @Bean
    @Order(value = 0)
    @ConditionalOnProperty(name = "shoulder.web.handleGlobalException", havingValue = "true", matchIfMissing = true)
    public RestControllerExceptionAdvice restControllerExceptionAdvice(){
        return new RestControllerExceptionAdvice();
    }

    /**
     * RestController 全局异常处理器
     * 默认情况下该类优先用户自定义的全局处理器，如果使用者指定 @Order且小于0，则优先于本框架处理
     */
    @Bean
    @Order(value = 0)
    @ConditionalOnProperty(name = "shoulder.web.unionResponse", havingValue = "true", matchIfMissing = true)
    public RestControllerUnionResponseAdvice restControllerUnionResponseAdvice(){
        return new RestControllerUnionResponseAdvice();
    }

    /**
     * RestController 接口调用汇报器（打印调用日志）【用于开发态】日志打在多行，且带颜色，代码跳转
     * 默认情况下该类优先用户自定义的全局处理器，如果使用者指定 @Order且小于0，则优先于本框架处理
     */
    @Bean
    @Order(value = 0)
    @ConditionalOnProperty(name = "shoulder.web.logRequest", havingValue = "colorful", matchIfMissing = true)
    public RestControllerColorfulLogAspect restControllerColorfulLogAspect(){
        return new RestControllerColorfulLogAspect(true);
    }

    /**
     * RestController 接口调用汇报器（打印调用日志）【用于生产态】日志打在一行中
     * 默认情况下该类优先用户自定义的全局处理器，如果使用者指定 @Order且小于0，则优先于本框架处理
     */
    @Bean
    @Order(value = 0)
    @ConditionalOnProperty(name = "shoulder.web.logRequest", havingValue = "json")
    public RestControllerJsonLogAspect restControllerJsonLogAspect(){
        return new RestControllerJsonLogAspect(true);
    }

}
