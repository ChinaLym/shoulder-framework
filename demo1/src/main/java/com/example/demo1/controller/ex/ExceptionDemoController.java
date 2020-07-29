package com.example.demo1.controller.ex;

import lombok.extern.shoulder.SLog;
import org.shoulder.core.exception.BaseRuntimeException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 异常示例
 *      自动全局捕获抛出的 {@link org.shoulder.core.exception.BaseRuntimeException}异常，会自动记录日志，并返回对应的返回值
 *
 * @author lym
 */
@SLog
@RestController
@RequestMapping("exception")
public class ExceptionDemoController {

    /**
     * 打印日志
     */
    @GetMapping("case1")
    public String messageSource(){
        foo();
        return "exception already.";
    }

    // todo 补充更多异常、错误码使用方式

    private void foo(){
        throw new BaseRuntimeException("demo ex");
    }

}
