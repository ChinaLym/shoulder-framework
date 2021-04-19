package com.example.demo1.controller.ex;

import com.example.demo1.ex.MyEx1;
import com.example.demo1.ex.MyEx2;
import org.shoulder.autoconfigure.web.WebAdvanceAutoConfiguration;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 异常示例
 * 自动全局捕获抛出的 {@link BaseRuntimeException}异常，会自动记录日志，并返回对应的返回值
 *
 * @author lym
 * @see WebAdvanceAutoConfiguration#restControllerExceptionAdvice 框架实现
 */
@RestController
@RequestMapping("exception")
public class ExceptionDemoController {


    private static final Logger log = LoggerFactory.getLogger(ExceptionDemoController.class);

    // --------------------------- 全局异常处理 ------------------------------

    /**
     * 不优雅的写法举例  <a href="http://localhost:8080/exception/0?kind=1" />
     * <a href="http://localhost:8080/exception/0?kind=2" />
     * 分类处理异常、记录日志
     */
    @GetMapping("0")
    public BaseResult<String> notRecommended(@RequestParam(required = false, defaultValue = "1") int kind) {
        try {
            String businessResult = businessMethod(kind);
            return BaseResult.success(businessResult);
        } catch (Exception e) {
            // 根据异常分类
            BaseResult<String> errorResponse = new BaseResult<>();
            if (e instanceof MyEx1) {
                // 记录 error 级别的日志，返回 500 错误码
                log.errorWithErrorCode("0x000a01", "发生了一个异常", e);
                errorResponse.setCode("0x000a01");
                errorResponse.setMsg(e.getMessage());
            } else if (e instanceof MyEx2) {
                // 记录 warn 级别的日志，返回 400 错误码
                log.warnWithErrorCode("0x000a02", "发生了一个很神奇的异常", e);
                errorResponse.setCode("0x000a02");
                errorResponse.setMsg(e.getMessage());
            }
            return errorResponse;
        }

    }

    /**
     * 使用 shoulder 框架：不需要管异常，框架会自动记录日志与包装返回值 <a href="http://localhost:8080/exception/1?kind=1" />
     * <a href="http://localhost:8080/exception/1?kind=2" />
     */
    @GetMapping("1")
    public String case1(@RequestParam(required = false, defaultValue = "1") int kind) {
        return businessMethod(kind);
    }


    /**
     * 模拟一个会抛出多种异常的业务方法
     */
    private String businessMethod(int kind) {
        if (kind == 1) {
            throw new MyEx1("0x000a01", "demo ex1");
        } else {
            throw new MyEx2("0x000a02", "demo ex2");
        }

    }


}
