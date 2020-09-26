package com.example.demo3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 尝试进入主页 http://localhost:8080/ 因引入了框架的认证，若未登录会自动跳转至认证页面，认证通过后才可访问
 *
 * @author lym
 */
@SpringBootApplication
public class Demo3Application {

    public static void main(String[] args) {
        SpringApplication.run(Demo3Application.class, args);
    }

}
