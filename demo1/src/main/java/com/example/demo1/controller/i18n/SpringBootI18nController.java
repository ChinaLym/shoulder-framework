package com.example.demo1.controller.i18n;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

/**
 * SpringBootI18nController
 *
 * shoulder 100%兼容 Spring Boot，查看 import 并没有引入 shoulder 的任何类
 *
 * @author lym
 */
@RestController
@RequestMapping("springboot/i18n")
public class SpringBootI18nController {

    @Autowired
    private MessageSource messageSource;

    /**
     * Spring boot 中的使用
     * @param toBeTranslate 待翻译的
     * @param args 用于填充翻译的参数
     * @param locale 语言
     * @return 翻译后的
     */
    @GetMapping("translate")
    public String messageSource(String toBeTranslate, String args, String locale){

        Locale aimLocale = StringUtils.parseLocale(locale);
        if(aimLocale == null){
            aimLocale = Locale.getDefault();
        }
        String[] trArgs = null;
        if(!StringUtils.isEmpty(args)){
            trArgs = args.split(",");
        }
        return messageSource.getMessage(toBeTranslate, trArgs, aimLocale);
    }

}
