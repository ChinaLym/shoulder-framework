package com.example.demo1.controller.response;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统一返回值类型示例
 *      自动包装返回值为标准返回值
 *      可以跳过包装
 *
 * @author lym
 */
@RestController
@RequestMapping("response")
public class ResponseDemoController {

    /**
     * 打印日志
     */
    @GetMapping("case1")
    public String messageSource(){
        return "data";
    }

}
