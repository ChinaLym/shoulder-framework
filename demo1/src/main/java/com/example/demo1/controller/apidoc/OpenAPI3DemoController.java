package com.example.demo1.controller.apidoc;

import com.example.demo1.dto.ApiDocV2;
import com.example.demo1.dto.ApiDocV3;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
     * <a href="http://localhost:8080/apidoc/1"/>
     */
    @Operation(description = "最简单的无参数")
    @GetMapping("1")
    public String case1() {
        return "xxxx";
    }

    /**
     * <a href="http://localhost:8080/apidoc/2"/>
     */
    @Operation(description = "参数放到url上")
    @GetMapping("2")
    public String case2(ApiDocV2 param) {
        return "xxxx";
    }

    @Operation(description = "参数放到 body 中")
    @PostMapping(value = "3", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String case3(@RequestBody ApiDocV2 param) {
        return "xxxx";
    }

    @Operation(description = "OpenApi3")
    @PostMapping(value = "4", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String case4(@RequestBody ApiDocV3 param) {
        return "xxxx";
    }

}
