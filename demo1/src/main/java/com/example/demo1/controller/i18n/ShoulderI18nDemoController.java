package com.example.demo1.controller.i18n;

import org.shoulder.core.context.BaseContextHolder;
import org.shoulder.core.i18.ShoulderMessageSource;
import org.shoulder.core.i18.Translator;
import org.shoulder.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

/**
 * DemoController
 *
 * @author lym
 */
@RestController
@RequestMapping("i18n")
public class ShoulderI18nDemoController {


    /**
     * Spring 定义的接口，shoulder 100%兼容
     */
    @Autowired
    private MessageSource messageSource;

    /**
     * 实际上与 messageSource 为同一个对象，具体实现类都是 {@link ShoulderMessageSource}
     */
    @Autowired
    private Translator translator;

    /**
     * Spring 中提供的用法
     *
     * @param toBeTranslate 待翻译的
     * @param args 用于填充翻译的参数
     * @param locale 语言
     * @return 翻译后的
     */
    @GetMapping("spring")
    public String messageSource(String toBeTranslate, String args, String locale){

        Locale aimLocale = org.springframework.util.StringUtils.parseLocale(locale);
        if(aimLocale == null){
            aimLocale = Locale.getDefault();
        }
        String[] trArgs = null;
        if(!org.springframework.util.StringUtils.isEmpty(args)){
            trArgs = args.split(",");
        }
        return messageSource.getMessage(toBeTranslate, trArgs, aimLocale);
    }


    /**
     * Shoulder 中的推荐用法
     * 在 Spring 之上简化了使用，不必再填写 Locale，将自动获取
     *
     * @param toBeTranslate 待翻译的
     * @param args 用于填充翻译的参数
     * @param locale 语言
     * @return 翻译后的
     */
    @GetMapping("shoulder")
    public String translator(String toBeTranslate, String args, String locale){
        Locale aimLocale = StringUtils.parseLocale(locale, Locale.getDefault());
        String[] trArgs = StringUtils.isNotEmpty(args) ? args.split(",") : null;

        return translator.getMessage(toBeTranslate, trArgs, aimLocale);
    }

    @GetMapping("1")
    public String case1(){
        // spring:
        messageSource.getMessage("shoulder.test.hi", null, BaseContextHolder.getLocale());

        // shoulder
        return translator.getMessage("shoulder.test.hi");
    }

    @GetMapping("2")
    public String case2(){
        // spring:
        Object[] args = new Object[1];
        args[0] = "shoulder";
        messageSource.getMessage("shoulder.test.hi", args, BaseContextHolder.getLocale());

        // shoulder
        return translator.getMessage("shoulder.test.hello", "shoulder");
    }


}
