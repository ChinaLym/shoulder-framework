package org.shoulder.ext.config.config;

import org.shoulder.core.log.InvokeLogInterceptor;
import org.shoulder.ext.common.constant.ShoulderExtConstants;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class BackstageInterceptorConfig {

    private static final String EXPRESSION
            = "execution(public org.shoulder.ext.config.provider.controller.controller.ParameterConfigController.*(..))";


    @Bean
    public static DefaultPointcutAdvisor invokeLogInterceptorAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(EXPRESSION);

        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(pointcut);
        advisor.setAdvice(new InvokeLogInterceptor(ShoulderExtConstants.BACKSTAGE_DIGEST_LOGGER));
        return advisor;
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public static DefaultPointcutAdvisor backstageExceptionInterceptorAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(EXPRESSION);

        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(pointcut);
        advisor.setAdvice(new ExceptionInterceptor());
        return advisor;
    }

}