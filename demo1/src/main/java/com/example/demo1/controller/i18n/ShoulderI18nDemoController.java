package com.example.demo1.controller.i18n;

import org.shoulder.core.context.BaseContextHolder;
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
@RequestMapping("shoulder/i18n")
public class ShoulderI18nDemoController {


    @Autowired
    private MessageSource messageSource;

    /**
     * 实际上与 messageSource 是同一个对象，额外提供了两个方法，
     * 不需要在代码中传对应语言标识的（默认使用 {@link BaseContextHolder#getLocale}），以 AOP 的思想简化代码编写
     */
    @Autowired
    private Translator translator;

    /**
     * Shoulder 中扩展 Spring boot 的使用，不必再填写 Locale，将自动获取
     * @param toBeTranslate 待翻译的
     * @param args 用于填充翻译的参数
     * @param locale 语言
     * @return 翻译后的
     */
    @GetMapping("translate")
    public String translator(String toBeTranslate, String args, String locale){
        Locale aimLocale = StringUtils.toLocale(locale, Locale.getDefault());
        String[] trArgs = StringUtils.isNotEmpty(args) ? args.split(",") : null;

        return translator.getMessage(toBeTranslate, trArgs, aimLocale);
    }

    @GetMapping("case1")
    public String translator(){
        return translator.getMessage("shoulder.test.hi");
    }


}
