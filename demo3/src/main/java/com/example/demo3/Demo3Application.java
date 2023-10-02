package com.example.demo3;

import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.security.authentication.BeforeAuthEndpoint;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;

/**
 * 尝试进入主页 http://localhost:8080/ 因引入了框架的认证，若未登录会自动跳转至认证页面，认证通过后才可访问
 *
 * @see BeforeAuthEndpoint 认证前会跳到这里先
 * @author lym
 */
@SpringBootApplication
public class Demo3Application {

    public static void main(String[] args) {
        AssertUtils.isTrue(false, CommonErrorCodeEnum.CODING, "0.7 版本不包含 demo3，可以去 0.6 分支查看使用");
        SpringApplication.run(Demo3Application.class, args);
    }

    @Bean
    AccessDeniedHandlerImpl accessDeniedHandler(){
        return new AccessDeniedHandlerImpl();

    }
}
