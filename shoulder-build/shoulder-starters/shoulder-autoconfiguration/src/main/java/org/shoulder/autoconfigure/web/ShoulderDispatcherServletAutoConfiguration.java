package org.shoulder.autoconfigure.web;

import org.shoulder.web.ShoulderDispatcherServlet;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * 替换为 ShoulderDispatcherServlet
 *
 * @author lym
 * @see DispatcherServletAutoConfiguration
 * @see ShoulderDispatcherServlet
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(DispatcherServletAutoConfiguration.class)
//@Conditional(DispatcherServletAutoConfiguration.DefaultDispatcherServletCondition.class)
@ConditionalOnClass(ShoulderDispatcherServlet.class)
@EnableConfigurationProperties(WebMvcProperties.class)
public class ShoulderDispatcherServletAutoConfiguration {

    @Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
    public DispatcherServlet dispatcherServlet(WebMvcProperties webMvcProperties) {
        DispatcherServlet dispatcherServlet = new ShoulderDispatcherServlet();
        dispatcherServlet.setDispatchOptionsRequest(webMvcProperties.isDispatchOptionsRequest());
        dispatcherServlet.setDispatchTraceRequest(webMvcProperties.isDispatchTraceRequest());
        dispatcherServlet.setThrowExceptionIfNoHandlerFound(webMvcProperties.isThrowExceptionIfNoHandlerFound());
        dispatcherServlet.setPublishEvents(webMvcProperties.isPublishRequestHandledEvents());
        dispatcherServlet.setEnableLoggingRequestDetails(webMvcProperties.isLogRequestDetails());
        return dispatcherServlet;
    }


}
