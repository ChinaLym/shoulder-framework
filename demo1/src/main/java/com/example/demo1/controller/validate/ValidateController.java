package com.example.demo1.controller.validate;

import org.shoulder.core.dto.response.RestResult;
import org.shoulder.validate.annotation.Enum;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试上传文件，合法校验
 * <a href="http://localhost:8080/" />
 *
 * @author lym
 */
@Validated
@RestController
@RequestMapping("validate")
public class ValidateController {


    /**
     * 正常写法举例，框架不会嵌套包装
     */
    @RequestMapping("0")
    public RestResult<String> notRecommended() {
        RestResult<String> response = new RestResult<>();
        response.setCode("0");
        response.setMsg("msg");
        response.setData("data");
        return response;
    }

    /**
     * 正常写法举例，框架不会嵌套包装
     * http://localhost:8080/validate/1?value=x
     */
    @RequestMapping("1")
    public String caseEnum(@Enum(enums = {"abc", "def"}) String value) {
        System.out.println(value);
        return "ok";
    }


}
