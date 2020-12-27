package com.example.demo1.controller.apidoc;

import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 介绍如何使用 OpenAPI 3 的注解
 *
 * @author lym
 */
@Api(tags = "api 文档 demo 相关接口")
@SkipResponseWrap
@RestController
@RequestMapping("apidoc")
public class OpenAPI3DemoController {


    /**
     * <a href="http://localhost:8080/apidoc/1"/> 最简单的无参数
     */
    @Operation(description = "case1")
    @GetMapping("1")
    public String case1() {
        return "xxxx";
    }

}
