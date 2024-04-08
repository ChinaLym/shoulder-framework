package org.shoulder.autoconfigure.web;

import org.shoulder.core.util.ContextUtils;
import org.shoulder.crypto.negotiation.cache.NegotiationResultCache;
import org.shoulder.crypto.negotiation.support.server.SensitiveRequestDecryptHandlerInterceptor;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.shoulder.web.common.CommonEndpoint;
import org.shoulder.web.interceptor.BaseRejectRepeatSubmitInterceptor;
import org.shoulder.web.interceptor.HttpLocaleInterceptor;
import org.shoulder.web.interceptor.SessionTokenRepeatSubmitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvcAutoConfiguration
 *
 * @author lym
 */
@AutoConfiguration
@ConditionalOnWebApplication
public class WebMvcAutoConfiguration {

    //@Bean fixme 日期格式 shoulder / spring 配置一致性
    //public Object justDoNothing(WebMvcProperties mvcProperties) {
    //    Format format = mvcProperties.getFormat();
    //    boolean matchSpringConfig = AppInfo.dateTimeFormat().equals(format.getDateTime());
    //    if(!matchSpringConfig) {
    //        ShoulderLoggers.SHOULDER_CONFIG;
    //            .warn("Configuration inconsistent detected: spring.mvc.format.datetime, not match "
    //                  + BaseAppProperties.dateTimeFormatConfigPath
    //                  + ", use spring config first! please keep the configuration consistent.");
    //    }
    //    WebConversionService conversionService = new WebConversionService(
    //        new DateTimeFormatters().dateFormat(format.getDate())
    //            .timeFormat(format.getTime())
    //            .dateTimeFormat(format.getDateTime()));
    //    return new Object();
    //}

    @AutoConfiguration
    @ConditionalOnClass(CommonEndpoint.class)
    protected static class CommonEndpointWebConfig {
        @Bean
        @ConditionalOnProperty(name = "shoulder.web.enableCommonEndpoint", havingValue = "true", matchIfMissing = true)
        public CommonEndpoint commonEndpoint() {
            return new CommonEndpoint();
        }
    }

    @AutoConfiguration
    @ConditionalOnClass(HttpLocaleInterceptor.class)
    protected static class LocaleInterceptorWebConfig implements WebMvcConfigurer {
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new HttpLocaleInterceptor()).order(Ordered.HIGHEST_PRECEDENCE);
            WebMvcConfigurer.super.addInterceptors(registry);
        }
    }

    @AutoConfiguration
    @ConditionalOnClass(SensitiveRequestDecryptHandlerInterceptor.class)
    protected static class NegotiationInterceptorWebConfig implements WebMvcConfigurer {

        @Lazy
        @Autowired
        private NegotiationResultCache negotiationResultCache;

        @Lazy
        @Autowired
        private TransportCryptoUtil transportCryptoUtil;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new SensitiveRequestDecryptHandlerInterceptor(negotiationResultCache, transportCryptoUtil)).order(Ordered.HIGHEST_PRECEDENCE);
            WebMvcConfigurer.super.addInterceptors(registry);
        }
    }

    @AutoConfiguration
    @ConditionalOnClass(SessionTokenRepeatSubmitInterceptor.class)
    @ConditionalOnProperty(name = "shoulder.web.repeatSubmit.enable", havingValue = "true", matchIfMissing = true)
    protected static class RejectRepeatSubmitWebConfig implements WebMvcConfigurer {

        @Bean
        @ConditionalOnMissingBean(BaseRejectRepeatSubmitInterceptor.class)
        public SessionTokenRepeatSubmitInterceptor rejectRepeatSubmitInterceptor(
            @Value("${shoulder.web.waf.repeatSubmit.requestTokenName:__repeat_token}") String requestTokenName,
            @Value("${shoulder.web.waf.repeatSubmit.sessionTokenName:__repeat_token}") String sessionTokenName
        ) {
            return new SessionTokenRepeatSubmitInterceptor(requestTokenName, sessionTokenName);
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            BaseRejectRepeatSubmitInterceptor rejectRepeatSubmitInterceptor = ContextUtils.getBeanOrNull(BaseRejectRepeatSubmitInterceptor.class);
            if (rejectRepeatSubmitInterceptor != null) {
                registry.addInterceptor(rejectRepeatSubmitInterceptor).order(Ordered.HIGHEST_PRECEDENCE);
                WebMvcConfigurer.super.addInterceptors(registry);
            }
        }
    }

}
