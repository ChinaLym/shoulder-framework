package com.example.demo3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 尝试访问 http://localhost:8080/，尝试进入主页，但因引入了框架的认证，故需要登录认证后才行
 *
 * @author lym
 */
@SpringBootApplication//(exclude = I18nAutoConfiguration.class)
public class Demo3Application {

    public static void main(String[] args) {
        SpringApplication.run(Demo3Application.class, args);
    }

}
