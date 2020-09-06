package com.example.demo2.controller.base;

import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 异常自动捕获与包装，不会把异常栈抛给前端，Shoulder 默认自动保护服务器的安全
 *
 * @author lym
 */
@SkipResponseWrap
@RestController
@RequestMapping("ex")
public class SecurityExceptionController {

    /**
     * http://localhost:8080/ex/jdkRuntime
     */
    @RequestMapping("jdkRuntime")
    public String jdkRuntime() {
        throw new RuntimeException();
    }

    /**
     * http://localhost:8080/ex/base
     */
    @RequestMapping("base")
    public String base() {
        throw new BaseRuntimeException("xxx");
    }

    /**
     * http://localhost:8080/ex/thread
     */
    @RequestMapping("thread")
    public String thread() {
        new Thread(() -> {
            throw new RuntimeException();
        }).run();
        return "";
    }
}
