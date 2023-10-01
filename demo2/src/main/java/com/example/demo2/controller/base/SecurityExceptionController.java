package com.example.demo2.controller.base;

import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 异常自动捕获与包装，该Controller 用于展示错误码，故接口都会返回错误码～～～不是报错哦～～
 * Shoulder 不会把接口中出现异常的栈抛给前端，默认自动保护服务器的安全
 *
 * @author lym
 */
@SkipResponseWrap
@RestController
@RequestMapping("ex")
public class SecurityExceptionController {

    /**
     * 代码里抛出没有 extends RuntimeException 或 implements ErrorCode 的异常，也不会打印堆栈给前端
     * 常见于引入第三方 jar / 二方jar，工具类等，但异常处理不完善，意外的抛给了前端
     * http://localhost:8080/ex/jdkRuntime
     *
     * @return {"code":"0x0000012c","msg":"UNKNOWN ERROR.","data":null}
     */
    @RequestMapping("jdkRuntime")
    public String jdkRuntime() {
        throw new RuntimeException();
    }

    /**
     * 异常 message 设置为 xxx
     * http://localhost:8080/ex/base
     * fixme 没catch？
     */
    @RequestMapping("base")
    public String base() {
        throw new BaseRuntimeException("customer ex message.");
    }

    /**
     * http://localhost:8080/ex/thread
     * fixme 没catch？
     */
    @RequestMapping("thread")
    public String thread() {
        new Thread(() -> {
            throw new RuntimeException();
        }).run();
        return "";
    }
}
