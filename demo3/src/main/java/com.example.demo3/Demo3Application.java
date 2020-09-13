package com.example.demo3;

import org.shoulder.autoconfigure.core.I18nAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author lym
 */
@SpringBootApplication(exclude = I18nAutoConfiguration.class)
public class Demo3Application {

    public static void main(String[] args) {
        SpringApplication.run(Demo3Application.class, args);
    }

}
