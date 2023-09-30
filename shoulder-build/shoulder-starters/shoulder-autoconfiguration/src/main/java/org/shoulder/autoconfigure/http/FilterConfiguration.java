package org.shoulder.autoconfigure.http;

import org.shoulder.web.filter.CleanContextFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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

    @Bean
    public FilterRegistrationBean<CleanContextFilter> requestFilterRegistration() {
        FilterRegistrationBean<CleanContextFilter> registration = new FilterRegistrationBean<>();
        // 将过滤器配置到FilterRegistrationBean对象中
        registration.setFilter(new CleanContextFilter());
        // 给过滤器取名
        registration.setName("cleanContextFilter");
        // 设置过滤器优先级，该值越小越优先被执行
        registration.setOrder(Integer.MIN_VALUE);
        List<String> urlPatterns = new ArrayList<>();
        urlPatterns.add("/*");
        // 设置urlPatterns参数
        registration.setUrlPatterns(urlPatterns);
        return registration;
    }
}
