package com.example.demo1.controller.apidoc;

import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.shoulder.crypto.negotiation.support.Sensitive;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "sensitive 相关接口")
@SkipResponseWrap
@RestController
@RequestMapping("sensitive")
public class SensitiveApiController {


    /**
     * <a href="http://localhost:8080/sensitive/1"/> 最简单的无参数
     */
    @Sensitive(sensitiveRequest = false, sensitiveResponse = false)
    @Operation(description = "mySensitiveTest")
    @GetMapping("1")
    public String mySensitiveTest() {
        return "xxxx";
    }

}
