package org.shoulder.autoconfigure.http;

import org.shoulder.web.filter.CleanContextFilter;
import org.shoulder.web.filter.DefaultTenantFilter;
import org.shoulder.web.filter.MockUserFilter;
import org.shoulder.web.filter.TraceFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lym
 */
@AutoConfiguration
@ConditionalOnClass(CleanContextFilter.class)
public class FilterConfiguration {

    public FilterConfiguration() {
        // just for debug
    }

    private static final int FILTER_THRESHOLD_DIFFERENCE = 10;

    public static enum DefaultWebFilterType {
        CLEAN_CONTEXT("cleanContextFilter"),
        TRACE("traceFilter"),
        DEFAULT_TENANT("defaultTenantFilter"),
        MOCK_USER("mockUserFilter");
        private final String name;

        DefaultWebFilterType(String name) {
            this.name = name;
        }
        public int calculateFilterOrder() {
            return Integer.MIN_VALUE + FILTER_THRESHOLD_DIFFERENCE * this.ordinal();
        }
    }

    @Bean
    public FilterRegistrationBean<CleanContextFilter> requestFilterRegistration() {
        FilterRegistrationBean<CleanContextFilter> registration = new FilterRegistrationBean<>();
        // 将过滤器配置到FilterRegistrationBean对象中
        registration.setFilter(new CleanContextFilter());
        // 给过滤器取名
        registration.setName(DefaultWebFilterType.CLEAN_CONTEXT.name);
        // 设置过滤器优先级，该值越小越优先被执行
        registration.setOrder(DefaultWebFilterType.CLEAN_CONTEXT.calculateFilterOrder());
        List<String> urlPatterns = new ArrayList<>();
        urlPatterns.add("/*");
        // 设置urlPatterns参数
        registration.setUrlPatterns(urlPatterns);
        return registration;
    }

    @Bean
    @ConditionalOnProperty(value = "shoulder.web.filter.trace.uuidIfMissing", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<TraceFilter> traceFilterRegistration() {
        FilterRegistrationBean<TraceFilter> registration = new FilterRegistrationBean<>();
        // 将过滤器配置到FilterRegistrationBean对象中
        registration.setFilter(new TraceFilter());
        // 给过滤器取名
        registration.setName(DefaultWebFilterType.TRACE.name);
        // 设置过滤器优先级，该值越小越优先被执行
        registration.setOrder(DefaultWebFilterType.TRACE.calculateFilterOrder());
        List<String> urlPatterns = new ArrayList<>();
        urlPatterns.add("/*");
        // 设置urlPatterns参数
        registration.setUrlPatterns(urlPatterns);
        return registration;
    }
    @Bean
    @ConditionalOnProperty(value = "shoulder.web.filter.tenant.default", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<DefaultTenantFilter> defaultTenantFilterRegistration(@Value("${shoulder.web.filter.tenant.default:'DEFAULT'}") String tenantCode) {
        FilterRegistrationBean<DefaultTenantFilter> registration = new FilterRegistrationBean<>();
        // 将过滤器配置到FilterRegistrationBean对象中
        registration.setFilter(new DefaultTenantFilter(tenantCode));
        // 给过滤器取名
        registration.setName(DefaultWebFilterType.DEFAULT_TENANT.name);
        // 设置过滤器优先级，该值越小越优先被执行
        registration.setOrder(DefaultWebFilterType.DEFAULT_TENANT.calculateFilterOrder());
        List<String> urlPatterns = new ArrayList<>();
        urlPatterns.add("/*");
        // 设置urlPatterns参数
        registration.setUrlPatterns(urlPatterns);
        return registration;
    }

    @Bean
    @ConditionalOnProperty(value = "shoulder.web.filter.userId.mockIfMissing", havingValue = "true", matchIfMissing = false)
    public FilterRegistrationBean<MockUserFilter> mockUserFilterRegistration() {
        FilterRegistrationBean<MockUserFilter> registration = new FilterRegistrationBean<>();
        // 将过滤器配置到FilterRegistrationBean对象中
        registration.setFilter(new MockUserFilter());
        // 给过滤器取名
        registration.setName(DefaultWebFilterType.MOCK_USER.name);
        // 设置过滤器优先级，该值越小越优先被执行
        registration.setOrder(DefaultWebFilterType.MOCK_USER.calculateFilterOrder());
        List<String> urlPatterns = new ArrayList<>();
        urlPatterns.add("/*");
        // 设置urlPatterns参数
        registration.setUrlPatterns(urlPatterns);
        return registration;
    }
}
