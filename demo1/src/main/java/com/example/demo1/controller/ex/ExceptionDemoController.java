package com.example.demo1.controller.ex;

import com.example.demo1.ex.MyEx1;
import com.example.demo1.ex.MyEx2;
import org.shoulder.autoconfigure.web.WebAdvanceAutoConfiguration;
import org.shoulder.core.dto.response.BaseResponse;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 异常示例
 *      自动全局捕获抛出的 {@link BaseRuntimeException}异常，会自动记录日志，并返回对应的返回值
 *
 * @see WebAdvanceAutoConfiguration#restControllerExceptionAdvice 框架实现
 *
 * @author lym
 */
@RestController
@RequestMapping("exception")
public class ExceptionDemoController {


    private static final Logger log = LoggerFactory.getLogger(ExceptionDemoController.class);

    // --------------------------- 全局异常处理 ------------------------------

    /**
     * 不优雅的写法举例  <a href="http://localhost:8080/exception/0" />
     * 分类处理异常、记录日志
     */
    @GetMapping("0")
    public BaseResponse<String> notRecommended(){
        try{
            String businessResult = businessMethod();
            return BaseResponse.success(businessResult);
        } catch (Exception e){
            // 根据异常分类
            BaseResponse<String> errorResponse = new BaseResponse<>();
            if(e instanceof MyEx1){
                // 记录 error 级别的日志，返回 500 错误码
                log.errorWithErrorCode("xxxxx1", "发生了一个异常", e);
                errorResponse.setCode("xxxxx1");
                errorResponse.setMsg(e.getMessage());
            }else if(e instanceof MyEx2){
                // 记录 warn 级别的日志，返回 400 错误码
                log.warnWithErrorCode("xxxxx1", "发生了一个很神奇的异常", e);
                errorResponse.setCode("xxxxx2");
                errorResponse.setMsg(e.getMessage());
            }
            return errorResponse;
        }

    }

    /**
     * 使用 shoulder 框架：不需要管异常，框架会自动记录日志与包装返回值 <a href="http://localhost:8080/exception/1" />
     */
    @GetMapping("1")
    public String case1(){
        return businessMethod();
    }

    /**
     * 模拟一个会抛出多种异常的业务方法
     */
    private String businessMethod(){
        boolean fakerRandom = (((int)System.currentTimeMillis()) ^ 1 ) == 0;
        if(fakerRandom){
            throw new MyEx1("0xaaa01", "demo ex1");
        } else {
            throw new MyEx2("0xaaa02", "demo ex2");
        }

    }


}
