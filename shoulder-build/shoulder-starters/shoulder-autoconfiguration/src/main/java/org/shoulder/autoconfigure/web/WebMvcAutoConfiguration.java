package org.shoulder.autoconfigure.web;

import org.shoulder.web.interceptor.HttpLocaleInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvcAutoConfiguration
 *
 * @author lym
 */
@Configuration
@ConditionalOnWebApplication
public class WebMvcAutoConfiguration {

    @Configuration
    @ConditionalOnClass(HttpLocaleInterceptor.class)
    protected static class LocaleInterceptorWebConfig implements WebMvcConfigurer {
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new HttpLocaleInterceptor()).order(Ordered.HIGHEST_PRECEDENCE);
            WebMvcConfigurer.super.addInterceptors(registry);
        }
    }

}
