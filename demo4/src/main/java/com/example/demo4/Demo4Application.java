package com.example.demo4;

import org.shoulder.autoconfigure.core.I18nAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * demo4
 *
 * @author lym
 */
@SpringBootApplication(exclude = I18nAutoConfiguration.class)
public class Demo4Application {

    public static void main(String[] args) {
        SpringApplication.run(Demo4Application.class, args);
    }

}
