package org.shoulder.autoconfigure.security.code;

import org.shoulder.autoconfigure.condition.ConditionalOnAuthType;
import org.shoulder.code.ValidateCodeFilter;
import org.shoulder.code.ValidateCodeProcessorHolder;
import org.shoulder.code.consts.ValidateCodeConsts;
import org.shoulder.code.controller.ValidateCodEndpoint;
import org.shoulder.code.processor.ValidateCodeProcessor;
import org.shoulder.code.store.ValidateCodeStore;
import org.shoulder.code.store.impl.MemoryValidateCodeRepository;
import org.shoulder.code.store.impl.RedisValidateCodeRepository;
import org.shoulder.code.store.impl.SessionValidateCodeRepository;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.security.authentication.AuthenticationType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
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
@ConditionalOnClass(ValidateCodeConsts.class)
@ConditionalOnBean(value = {ValidateCodeProcessor.class})
@Configuration(proxyBeanMethods = false)
public class ValidateCodeBeanConfig {

    /**
     * 验证码相关 spring security 配置类
     */
    @Bean
    public ValidateCodeSecurityConfig validateCodeSecurityConfig(ValidateCodeFilter validateCodeFilter) {
        return new ValidateCodeSecurityConfig(validateCodeFilter);
    }

    /**
     * 验证码处理器 Holder，此时一定有，因为 ConditionalOnBean
     * 并装配进默认的 FrameworkEndpoint 中
     */
    @Bean
    public ValidateCodeProcessorHolder validateCodeProcessorHolder(List<ValidateCodeProcessor> validateCodeProcessors) {
        return new ValidateCodeProcessorHolder(validateCodeProcessors);
    }

    @Bean
    @ConditionalOnBean(ValidateCodeProcessorHolder.class)
    @ConditionalOnProperty(value = ValidateCodeConsts.CONFIG_PREFIX + ".default-endpoint.enable", havingValue = "true", matchIfMissing = true)
    public ValidateCodEndpoint validateCodEndpoint(ValidateCodeProcessorHolder validateCodeProcessorHolder) {
        return new ValidateCodEndpoint(validateCodeProcessorHolder);
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

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnAuthType(type = AuthenticationType.SESSION)
    public ValidateCodeStore sessionValidateCodeRepository() {
        return new SessionValidateCodeRepository();
    }


    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(value = ValidateCodeStore.class)
    @ConditionalOnClass(RedisTemplate.class)
    @ConditionalOnAuthType(type = AuthenticationType.TOKEN)
    public static class RedisValidateCodeStoreConfiguration {

        @Bean
        public ValidateCodeStore redisValidateCodeRepository(RedisTemplate redisTemplate,
               @Value("${shoulder.security.auth.code.unionCodePramName:deviceId}") String unionCodePramName) {
            return new RedisValidateCodeRepository(redisTemplate, unionCodePramName);
        }
    }

    @Deprecated
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(value = ValidateCodeStore.class)
    @ConditionalOnMissingClass(value = "org.springframework.data.redis.core.RedisTemplate")
    @ConditionalOnAuthType(type = AuthenticationType.TOKEN)
    public static class MemoryValidateCodeStoreConfiguration {

        @Bean
        public ValidateCodeStore memoryValidateCodeRepository() {
            LoggerFactory.getLogger(getClass()).warn("MemoryValidateCodeRepository is a ValidateCodeStore just for test, not a production level implement.");
            return new MemoryValidateCodeRepository();
        }
    }



}
