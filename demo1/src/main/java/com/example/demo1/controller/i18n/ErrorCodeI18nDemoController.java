package com.example.demo1.controller.i18n;

import com.example.demo1.ex.code.DemoErrorCodeEnum;
import com.example.demo1.ex.code.LoginErrorCodeEnum;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.i18.Translator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 只需要开发错误码，错误码注释，即可实现急速翻译
 *
 * @author lym
 */
@RestController
@RequestMapping("i18n")
public class ErrorCodeI18nDemoController {

    @Autowired
    private Translator translator;

    /**
     * 错误码 ErrorCode 继承了 Translatable，间接继承了 MessageSourceResolvable，因此可以调用 Spring 的方法直接翻译
     * errcode-maven-plugin 可以在打包时可以根据代码注释的 @desc 生成错误码翻译文件（默认只生成中文）
     * 启动后，shoulder扩展的 messageSource 可以加载这些文件，即可实现翻译错误码为注释上的内容
     * shoulder 默认注入了，根据浏览器的请求头获取语言标识的拦截器，因此可以自动识别当前上下文应格是什么语言
     * shoulder 的Translator 在 Spring 的基础上扩展了根据上下文中的多语言标识翻译，因此不需要传语言标识
     * 最终实现了，只需要开发错误码，错误码注释，即可实现急速翻译
     *
     * <a href="http://localhost:8080/i18n/errorCode" />
     *
     * @return 翻译好的
     */
    @GetMapping("errorCode")
    public String errorCode() {
        System.out.println("--------------------");
        System.out.println(translator.getMessage(CommonErrorCodeEnum.FILE_CREATE_FAIL));
        System.out.println(translator.getMessage(CommonErrorCodeEnum.AUTH_401_EXPIRED));
        System.out.println(translator.getMessage(DemoErrorCodeEnum.AGE_OUT_OF_RANGE));
        System.out.println(translator.getMessage(DemoErrorCodeEnum.SIGN_UP_FAIL));
        System.out.println(translator.getMessage(LoginErrorCodeEnum.USER_LOCKED));
        System.out.println("--------------------");
        return "查看控制台";
    }


}
