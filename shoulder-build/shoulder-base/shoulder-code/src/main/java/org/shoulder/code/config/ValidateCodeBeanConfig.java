package org.shoulder.code.config;

import org.shoulder.code.ValidateCodeFilter;
import org.shoulder.code.ValidateCodeProcessorHolder;
import org.shoulder.code.controller.ValidateCodeController;
import org.shoulder.code.processor.ValidateCodeProcessor;
import org.shoulder.code.store.ValidateCodeStore;
import org.shoulder.code.store.impl.SessionValidateCodeRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.util.List;

/**
 * 装配验证码过滤器
 * <p>
 * ConditionalOnBean 需要容器中提供实现类，以及失败处理器之后才可以装配
 *
 * @author lym
 */
@ConditionalOnBean(value = {ValidateCodeProcessor.class})
@Configuration(
    proxyBeanMethods = false
)
public class ValidateCodeBeanConfig {

    /**
     * 验证码相关 spring security 配置类
     */
    @Bean
    public ValidateCodeSecurityConfig validateCodeSecurityConfig(ValidateCodeFilter validateCodeFilter) {
        return new ValidateCodeSecurityConfig(validateCodeFilter);
    }

    /**
     * 验证码处理器 Holder
     * 并装配进默认的 Controller 中
     */
    @Bean
    public ValidateCodeProcessorHolder validateCodeProcessorHolder(List<ValidateCodeProcessor> validateCodeProcessors,
                                                                   @Nullable ValidateCodeController validateCodeController) {
        ValidateCodeProcessorHolder validateCodeProcessorHolder = new ValidateCodeProcessorHolder(validateCodeProcessors);
        if (validateCodeController != null) {
            validateCodeController.setValidateCodeProcessorHolder(validateCodeProcessorHolder);
        }
        return validateCodeProcessorHolder;
    }

    /**
     * 验证码过滤器
     */
    @Bean
    public ValidateCodeFilter validateCodeFilter(ValidateCodeProcessorHolder validateCodeProcessorHolder,
                                                 @Nullable AuthenticationFailureHandler authenticationFailureHandler) {
        return new ValidateCodeFilter(authenticationFailureHandler, validateCodeProcessorHolder);
    }

    // ----------------- ValidateCodeStore 默认实现 --------------------

    @ConditionalOnMissingBean(ValidateCodeStore.class)
    @Configuration(
        proxyBeanMethods = false
    )
    public static class ValidateCodeStoreConfig {
        /*@ConditionalOnMissingBean(RedisTemplate.class)
        @ConditionalOnClass(RedisTemplate.class)
        @Bean
        public RedisTemplate redisTemplate(){
            return new RedisTemplate();
        }

        @Bean
        @ConditionalOnClass(RedisTemplate.class)
        public ValidateCodeStore RedisValidateCodeRepository(RedisTemplate redisTemplate){
            return new RedisValidateCodeRepository(redisTemplate);
        }*/

        @Bean
        @ConditionalOnMissingBean(ValidateCodeStore.class)
        public ValidateCodeStore sessionValidateCodeRepository() {
            return new SessionValidateCodeRepository();
        }
    }

}
